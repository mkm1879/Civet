/**
 * Copyright Nov 26, 2018 Michael K Martin
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.clemson.lph.civet.xml.elements;

/**
 * 
 */
public class Person {
	public String name;
	public NameParts nameParts;
	public String phone;
	public String email;

	public Person( String name, String phone, String email ) {
		this.name = name;
		this.phone = phone;
		this.email = email;
	}
	
	public Person( NameParts nameParts, String phone, String email ) {
		this.nameParts = nameParts;
		this.phone = phone;
		this.email = email;
	}
}
