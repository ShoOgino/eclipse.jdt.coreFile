/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package jpms.test.a;

import java.util.concurrent.Callable;
import java.util.function.Function;

public class TestFunction implements Function<String, Callable<String>> {

	@SuppressWarnings("unchecked")
	@Override
	public Callable<String> apply(String clazz) {
		try {
			Class<?> c = Class.forName(clazz);
			System.out.println("    SUCCESS: " + c + " from->" + c.getModule() + " SUPER: " + c.getSuperclass() + " from->" + c.getSuperclass().getModule());
			Object obj = c.getConstructor().newInstance();
			if (obj instanceof Callable) {
				return (Callable<String>) obj;
			}
			return () -> obj.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new RuntimeException("FAILED: " + clazz);
	}

}
