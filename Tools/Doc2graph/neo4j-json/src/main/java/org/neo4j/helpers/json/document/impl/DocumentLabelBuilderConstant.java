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

import org.neo4j.graphdb.Label;
import org.neo4j.helpers.json.document.DocumentLabelBuilder;

/**
 * Fixed label, ignore data and context
 * @author Omar Rampado
 *
 */
public class DocumentLabelBuilderConstant implements DocumentLabelBuilder {

	private Label label;

	/* (non-Javadoc)
	 * @see org.neo4j.helpers.json.document.DocumentLabelBuilder#buildLabel(java.util.Map)
	 */
	@Override
	public Label buildLabel(Map<String,Object> obj) {
		return this.label;
	}

	/**
	 * Configure label name
	 */
	@Override
	public void setDefaultLabel(String defaultLabel) {
		this.label = Label.label(defaultLabel);
	}

}
