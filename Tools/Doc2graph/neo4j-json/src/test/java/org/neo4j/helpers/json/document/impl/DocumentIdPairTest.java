/**
 * Copyright (c) 2017 LARUS Business Automation [http://www.larus-ba.it]
 * <p>
 * This file is part of the "LARUS Integration Framework for Neo4j".
 * <p>
 * The "LARUS Integration Framework for Neo4j" is licensed
 * under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.helpers.json.document.impl;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * @author Omar Rampado
 *
 */
public class DocumentIdPairTest {

	DocumentIdPair docId;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.docId = new DocumentIdPair("attr","name","artist", 10);
	}

	@Test
	public void testGetFields() {
		Map<String, String> fields = this.docId.getFields();
		Assert.assertEquals("artist", fields.get("attr"));
		Assert.assertEquals("10", fields.get("name"));
	}

	@Test
	public void testToCypherFilter() {
		String cypherFilter = this.docId.toCypherFilter();
		Assert.assertEquals("attr: 'artist', name: 10", cypherFilter);
	}

	@Test
	public void testToCypherFilterSpecial() {
		docId = new DocumentIdPair("attr","name","artist", "I'm the best");
		String cypherFilter = this.docId.toCypherFilter();
		Assert.assertEquals("attr: 'artist', name: 'I\\\'m the best'", cypherFilter);
	}
}
