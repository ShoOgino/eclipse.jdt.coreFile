/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.processing;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.internal.core.util.Util;

public abstract class JobManager implements Runnable {

	/* queue of jobs to execute */
	protected IJob[] awaitingJobs = new IJob[10];
	protected int jobStart = 0;
	protected int jobEnd = -1;
	protected boolean executing = false;

	/* background processing */
	protected Thread processingThread;

	/* flag indicating whether job execution is enabled or not */
	private boolean enabled = true;

	public static boolean VERBOSE = false;
	/* flag indicating that the activation has completed */
	public boolean activated = false;
	
	private int awaitingClients = 0;

	/**
	 * Invoked exactly once, in background, before starting processing any job
	 */
	public void activateProcessing() {
		this.activated = true;
	}
	/**
	 * Answer the amount of awaiting jobs.
	 */
	public synchronized int awaitingJobsCount() {
		// pretend busy in case concurrent job attempts performing before activated
		return this.activated ? this.jobEnd - this.jobStart + 1 : 1;
	}
	/**
	 * Answers the first job in the queue, or null if there is no job available
	 * Until the job has completed, the job manager will keep answering the same job.
	 */
	public synchronized IJob currentJob() {
		if (this.enabled && this.jobStart <= this.jobEnd)
			return this.awaitingJobs[this.jobStart];
		return null;
	}
	public void disable() {
		this.enabled = false;
		if (VERBOSE)
			Util.verbose("DISABLING background indexing"); //$NON-NLS-1$
	}
	/**
	 * Remove the index from cache for a given project.
	 * Passing null as a job family discards them all.
	 */
	public void discardJobs(String jobFamily) {

		if (VERBOSE)
			Util.verbose("DISCARD   background job family - " + jobFamily); //$NON-NLS-1$

		boolean wasEnabled = isEnabled();
		try {
			IJob currentJob;
			// cancel current job if it belongs to the given family
			synchronized(this){
				currentJob = this.currentJob();
				disable();
			}
			if (currentJob != null && (jobFamily == null || currentJob.belongsTo(jobFamily))) {
				currentJob.cancel();

				// wait until current active job has finished
				while (this.processingThread != null && this.executing){
					try {
						if (VERBOSE)
							Util.verbose("-> waiting end of current background job - " + currentJob); //$NON-NLS-1$ //$NON-NLS-2$
						Thread.sleep(50);
					} catch(InterruptedException e){
						// ignore
					}
				}
			}

			// flush and compact awaiting jobs
			int loc = -1;
			synchronized(this) {
				for (int i = this.jobStart; i <= this.jobEnd; i++) {
					currentJob = this.awaitingJobs[i];
					this.awaitingJobs[i] = null;
					if (!(jobFamily == null || currentJob.belongsTo(jobFamily))) { // copy down, compacting
						this.awaitingJobs[++loc] = currentJob;
					} else {
						if (VERBOSE)
							Util.verbose("-> discarding background job  - " + currentJob); //$NON-NLS-1$
						currentJob.cancel();
					}
				}
				this.jobStart = 0;
				this.jobEnd = loc;
			}
		} finally {
			if (wasEnabled)
				enable();
		}
		if (VERBOSE)
			Util.verbose("DISCARD   DONE with background job family - " + jobFamily); //$NON-NLS-1$
	}
	public synchronized void enable() {
		this.enabled = true;
		if (VERBOSE)
			Util.verbose("ENABLING  background indexing"); //$NON-NLS-1$
		this.notifyAll(); // wake up the background thread if it is waiting (context must be synchronized)			
	}
	public boolean isEnabled() {
		return this.enabled;
	}
	/**
	 * Advance to the next available job, once the current one has been completed.
	 * Note: clients awaiting until the job count is zero are still waiting at this point.
	 */
	protected synchronized void moveToNextJob() {
		//if (!enabled) return;

		if (this.jobStart <= this.jobEnd) {
			this.awaitingJobs[this.jobStart++] = null;
			if (this.jobStart > this.jobEnd) {
				this.jobStart = 0;
				this.jobEnd = -1;
			}
		}
	}
	/**
	 * When idle, give chance to do something
	 */
	protected void notifyIdle(long idlingTime) {
		// do nothing
	}
	/**
	 * This API is allowing to run one job in concurrence with background processing.
	 * Indeed since other jobs are performed in background, resource sharing might be 
	 * an issue.Therefore, this functionality allows a given job to be run without
	 * colliding with background ones.
	 * Note: multiple thread might attempt to perform concurrent jobs at the same time,
	 *            and should synchronize (it is deliberately left to clients to decide whether
	 *            concurrent jobs might interfere or not. In general, multiple read jobs are ok).
	 *
	 * Waiting policy can be:
	 * 		IJobConstants.ForceImmediateSearch
	 * 		IJobConstants.CancelIfNotReadyToSearch
	 * 		IJobConstants.WaitUntilReadyToSearch
	 *
	 */
	public boolean performConcurrentJob(IJob searchJob, int waitingPolicy, IProgressMonitor progress) {
		if (VERBOSE)
			Util.verbose("STARTING  concurrent job - " + searchJob); //$NON-NLS-1$

		searchJob.ensureReadyToRun();

		int concurrentJobWork = 100;
		if (progress != null)
			progress.beginTask("", concurrentJobWork); //$NON-NLS-1$
		boolean status = IJob.FAILED;
		if (awaitingJobsCount() > 0) {
			switch (waitingPolicy) {

				case IJob.ForceImmediate :
					if (VERBOSE)
						Util.verbose("-> NOT READY - forcing immediate - " + searchJob);//$NON-NLS-1$
					boolean wasEnabled = isEnabled();
					try {
						disable(); // pause indexing
						status = searchJob.execute(progress == null ? null : new SubProgressMonitor(progress, concurrentJobWork));
					} finally {
						if (wasEnabled)
							enable();
					}
					if (VERBOSE)
						Util.verbose("FINISHED  concurrent job - " + searchJob); //$NON-NLS-1$
					return status;

				case IJob.CancelIfNotReady :
					if (VERBOSE)
						Util.verbose("-> NOT READY - cancelling - " + searchJob); //$NON-NLS-1$
					if (progress != null) progress.setCanceled(true);
					if (VERBOSE)
						Util.verbose("CANCELED concurrent job - " + searchJob); //$NON-NLS-1$
					throw new OperationCanceledException();

				case IJob.WaitUntilReady :
					int awaitingWork;
					IJob previousJob = null;
					IJob currentJob;
					IProgressMonitor subProgress = null;
					int totalWork = this.awaitingJobsCount();
					if (progress != null && totalWork > 0) {
						subProgress = new SubProgressMonitor(progress, concurrentJobWork / 2);
						subProgress.beginTask("", totalWork); //$NON-NLS-1$
						concurrentJobWork = concurrentJobWork / 2;
					}
					// use local variable to avoid potential NPE (see bug 20435 NPE when searching java method
					// and bug 42760 NullPointerException in JobManager when searching)
					Thread t = this.processingThread;
					int originalPriority = t == null ? -1 : t.getPriority();
					try {
						if (t != null)
							t.setPriority(Thread.currentThread().getPriority());
						synchronized(this) {
							this.awaitingClients++;
						}
						while ((awaitingWork = awaitingJobsCount()) > 0) {
							if (subProgress != null && subProgress.isCanceled())
								throw new OperationCanceledException();
							currentJob = currentJob();
							// currentJob can be null when jobs have been added to the queue but job manager is not enabled
							if (currentJob != null && currentJob != previousJob) {
								if (VERBOSE)
									Util.verbose("-> NOT READY - waiting until ready - " + searchJob);//$NON-NLS-1$
								if (subProgress != null) {
									subProgress.subTask(
										Util.bind("manager.filesToIndex", Integer.toString(awaitingWork))); //$NON-NLS-1$
									subProgress.worked(1);
								}
								previousJob = currentJob;
							}
							try {
								if (VERBOSE)
									Util.verbose("-> GOING TO SLEEP - " + searchJob);//$NON-NLS-1$
								Thread.sleep(50);
							} catch (InterruptedException e) {
								// ignore
							}
						}
					} finally {
						synchronized(this) {
							this.awaitingClients--;
						}
						if (originalPriority > -1 && t.isAlive())
							t.setPriority(originalPriority);
					}
					if (subProgress != null)
						subProgress.done();
			}
		}
		status = searchJob.execute(progress == null ? null : new SubProgressMonitor(progress, concurrentJobWork));
		if (progress != null)
			progress.done();
		if (VERBOSE)
			Util.verbose("FINISHED  concurrent job - " + searchJob); //$NON-NLS-1$
		return status;
	}
	public abstract String processName();
	
	public synchronized void request(IJob job) {

		job.ensureReadyToRun();

		// append the job to the list of ones to process later on
		int size = this.awaitingJobs.length;
		if (++this.jobEnd == size) { // when growing, relocate jobs starting at position 0
			this.jobEnd -= this.jobStart;
			System.arraycopy(this.awaitingJobs, this.jobStart, this.awaitingJobs = new IJob[size * 2], 0, this.jobEnd);
			this.jobStart = 0;
		}
		this.awaitingJobs[this.jobEnd] = job;
		if (VERBOSE) {
			Util.verbose("REQUEST   background job - " + job); //$NON-NLS-1$
			Util.verbose("AWAITING JOBS count: " + awaitingJobsCount()); //$NON-NLS-1$
		}
		notifyAll(); // wake up the background thread if it is waiting
	}
	/**
	 * Flush current state
	 */
	public synchronized void reset() {
		if (VERBOSE)
			Util.verbose("Reset"); //$NON-NLS-1$

		if (this.processingThread != null) {
			discardJobs(null); // discard all jobs
		} else {
			/* initiate background processing */
			this.processingThread = new Thread(this, this.processName());
			this.processingThread.setDaemon(true);
			// less prioritary by default, priority is raised if clients are actively waiting on it
			this.processingThread.setPriority(Thread.NORM_PRIORITY-1); 
			this.processingThread.start();
		}
	}
	/**
	 * Infinite loop performing resource indexing
	 */
	public void run() {

		long idlingStart = -1;
		activateProcessing();
		try {
			class ProgressJob extends Job {
				ProgressJob(String name) {
					super(name);
				}
				protected IStatus run(IProgressMonitor monitor) {
					int awaitingJobsCount;
					while ((awaitingJobsCount = awaitingJobsCount()) > 0) {
						monitor.subTask(Util.bind("manager.filesToIndex", Integer.toString(awaitingJobsCount))); //$NON-NLS-1$
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// ignore
						}
					}
					return Status.OK_STATUS;
				}
			}
			ProgressJob progressJob = null;
			while (this.processingThread != null) {
				try {
					IJob job;
					synchronized (this) {
						// handle shutdown case when notifyAll came before the wait but after the while loop was entered
						if (this.processingThread == null) continue;

						// must check for new job inside this sync block to avoid timing hole
						if ((job = currentJob()) == null) {
							if (progressJob != null) progressJob = null;
							if (idlingStart < 0)
								idlingStart = System.currentTimeMillis();
							else
								notifyIdle(System.currentTimeMillis() - idlingStart);
							this.wait(); // wait until a new job is posted (or reenabled:38901)
						} else {
							idlingStart = -1;
						}
					}
					if (job == null) {
						notifyIdle(System.currentTimeMillis() - idlingStart);
						// just woke up, delay before processing any new jobs, allow some time for the active thread to finish
						Thread.sleep(500);
						continue;
					}
					if (VERBOSE) {
						Util.verbose(awaitingJobsCount() + " awaiting jobs"); //$NON-NLS-1$
						Util.verbose("STARTING background job - " + job); //$NON-NLS-1$
					}
					try {
						this.executing = true;
						if (progressJob == null) {
							progressJob = new ProgressJob(Util.bind("manager.indexingInProgress")); //$NON-NLS-1$
							progressJob.setPriority(Job.LONG);
							progressJob.setSystem(true);
							progressJob.schedule();
						}
						/*boolean status = */job.execute(null);
						//if (status == FAILED) request(job);
					} finally {
						this.executing = false;
						if (VERBOSE)
							Util.verbose("FINISHED background job - " + job); //$NON-NLS-1$
						moveToNextJob();
						if (this.awaitingClients == 0)
							Thread.sleep(50);
					}
				} catch (InterruptedException e) { // background indexing was interrupted
				}
			}
		} catch (RuntimeException e) {
			if (this.processingThread != null) { // if not shutting down
				// log exception
				Util.log(e, "Background Indexer Crash Recovery"); //$NON-NLS-1$
				
				// keep job manager alive
				this.discardJobs(null);
				this.processingThread = null;
				this.reset(); // this will fork a new thread with no waiting jobs, some indexes will be inconsistent
			}
			throw e;
		} catch (Error e) {
			if (this.processingThread != null && !(e instanceof ThreadDeath)) {
				// log exception
				Util.log(e, "Background Indexer Crash Recovery"); //$NON-NLS-1$
				
				// keep job manager alive
				this.discardJobs(null);
				this.processingThread = null;
				this.reset(); // this will fork a new thread with no waiting jobs, some indexes will be inconsistent
			}
			throw e;
		}
	}
	/**
	 * Stop background processing, and wait until the current job is completed before returning
	 */
	public void shutdown() {

		if (VERBOSE)
			Util.verbose("Shutdown"); //$NON-NLS-1$

		disable();
		discardJobs(null); // will wait until current executing job has completed
		Thread thread = this.processingThread;
		try {
			if (thread != null) { // see http://bugs.eclipse.org/bugs/show_bug.cgi?id=31858
				synchronized (this) {
					this.processingThread = null; // mark the job manager as shutting down so that the thread will stop by itself
					this.notifyAll(); // ensure its awake so it can be shutdown
				}
				// in case processing thread is handling a job
				thread.join();
			}
		} catch (InterruptedException e) {
			// ignore
		}
	}
	public String toString() {
		StringBuffer buffer = new StringBuffer(10);
		buffer.append("Enabled:").append(this.enabled).append('\n'); //$NON-NLS-1$
		int numJobs = this.jobEnd - this.jobStart + 1;
		buffer.append("Jobs in queue:").append(numJobs).append('\n'); //$NON-NLS-1$
		for (int i = 0; i < numJobs && i < 15; i++) {
			buffer.append(i).append(" - job["+i+"]: ").append(this.awaitingJobs[this.jobStart+i]).append('\n'); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return buffer.toString();
	}	
}
