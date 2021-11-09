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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.*;
import org.neo4j.helpers.json.document.DocumentGrapher;
import org.neo4j.helpers.json.document.DocumentId;
import org.neo4j.helpers.json.document.DocumentIdBuilder;
import org.neo4j.helpers.json.document.DocumentLabelBuilder;
import org.neo4j.helpers.json.document.DocumentRelationBuilder;
import org.neo4j.helpers.json.document.context.DocumentGrapherExecutionContext;
import org.neo4j.helpers.json.document.context.DocumentRelationContext;
import org.neo4j.logging.Log;

/**
 * Recursive implementation of logic to transform document into graph (tree)
 * @author Omar Rampado
 *
 */
public class DocumentGrapherRecursive implements DocumentGrapher {

	private GraphDatabaseService db;

	private Log log;

	private DocumentIdBuilder documentIdBuilder;
	private DocumentRelationBuilder documentRelationBuilder;
	private DocumentLabelBuilder documentLabelBuilder;
	private String rootNodeKeyProperty;
	private boolean logDiscadEvent;

	/**
	 * Init with context/configuration
	 * @param context
	 */
	public DocumentGrapherRecursive(DocumentGrapherExecutionContext context) {
		this.db = context.getDb();
		this.log = context.getLog();
		this.documentIdBuilder = context.getDocumentIdBuilder();
		this.documentRelationBuilder = context.getDocumentRelationBuilder();
		this.documentLabelBuilder = context.getDocumentLabelBuilder();
		this.rootNodeKeyProperty = context.getRootNodeKeyProperty();
		this.logDiscadEvent = context.isLogDiscard();
	}

	/**
	 * Simple POJO for complex output
	 */
	class RecursiveReturn
	{
		Map<String, Object> document;
		List<Node> childrenNodes;
		
		public RecursiveReturn() {
			document = new HashMap<>();
			childrenNodes = new ArrayList<>();
		}
	}
	
	@Override
	@SuppressWarnings({ "rawtypes"})
	public Node upsertDocument(String key, Map inDocument) {
		Transaction transaction = db.beginTx();
		Node root = upsertDocument(key, inDocument, 0, transaction);
		root.setProperty(rootNodeKeyProperty, key);
		transaction.commit();
		return root;
	}
	

	/**
	 * Internal implementation of upsertDocument
	 * @param key
	 * @param inDocument
	 * @param level
	 * @return
	 */
	@SuppressWarnings({ "rawtypes"})
	private Node upsertDocument(String key, Map inDocument, int level, Transaction transaction) {
		log.info("start of upsertDocument");
		RecursiveReturn recursiveResult = recursiveNavigation(key, inDocument, level, transaction);
		
		List<Node> childrenNodes = recursiveResult.childrenNodes;
		Map<String, Object> document = recursiveResult.document; 
		
		DocumentId documentId = this.documentIdBuilder.buildId(document);
		Label label = this.documentLabelBuilder.buildLabel(document);

		Node node = findNodeIntoGraphDb(label, documentId, transaction);
		log.info("before check node == null");
		//if not exists, it's time to create it!
		if(node == null)
		{
			log.info("before transaction.createNode(label)");
			node = transaction.createNode(label);
//			node = db.createNode(label);
			if(log.isDebugEnabled())
				log.debug("Create new "+label+" "+document);
		}

		//set properties
		final Node n = node;
		
		document.forEach((k, v) -> {
			if(v != null){
				n.setProperty(k, v);				
			}
		});
		
		DocumentRelationContext context = new DocumentRelationContext();
		context.setDocumentKey(key);
		
		//build relationship
		childrenNodes.forEach(child -> {
			documentRelationBuilder.buildRelation(n, child, context);
		});

		return node;
	}

	/**
	 * Search into database if exists a node with documentId
	 * @param label 
	 * @param documentId
	 * @return
	 */
	private Node findNodeIntoGraphDb(Label label, DocumentId documentId, Transaction transaction) {
		Node node = null;
		
		//check if node already exists
		String query = "MATCH (n:"+label.name()+" {"+documentId.toCypherFilter()+"}) RETURN n";
		if(log.isDebugEnabled())
		{
			log.debug(query);
		}
		
//		Result result = db.execute(query);
		Result result = transaction.execute(query);

		while (result.hasNext()) {
			Map<String, Object> row = result.next();
			node = (Node) row.get("n");
			if(log.isDebugEnabled())
			{
				log.debug("Found: "+node);
			}
		}

		return node;
	}

	/**
	 * Start recursive flow versus {@link #upsertDocument(String, Map, int, Transaction)}
	 * @param key
	 * @param inDocument
	 * @param level
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private RecursiveReturn recursiveNavigation(String key, Map inDocument, int level, Transaction transaction) {
		RecursiveReturn result = new RecursiveReturn();
		
		//extract child, recursive
		inDocument.forEach((k,v)->{
			String fieldKey = (String)k;
			
			//if value is a complex object (map)
			if(v instanceof Map)
			{
				Map inner = (Map)v;
				manageComplexType(key, result, inner, level, transaction);
			}else if(v instanceof List)
			{
				//if value is and array
				List list = (List)v;
				manageArrayType(key, result, fieldKey, list, level, transaction);
				
			}else
			{
				//if value is a primitive type
				result.document.put(fieldKey, v);
			}
		});
		
		return result;
	}

	/**
	 * Manage array of primitive or complex type
	 * @param key
	 * @param result
	 * @param fieldKey
	 * @param list
	 * @param level
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void manageArrayType(String key, RecursiveReturn result, String fieldKey, List list, int level, Transaction transaction) {
		if(!list.isEmpty())
		{
			Object object = list.get(0);
			//assumption: homogeneous array
			
			//if is an array of complex type
			if(object instanceof Map)
			{
				list.forEach(m -> {
					Map inner = (Map)m;
					manageComplexType(key, result, inner, level, transaction);
				});
			}else
			{
				//if is an array of primitive type
				result.document.put(fieldKey, list.stream()
						.map(o -> String.valueOf(o))
						.toArray(size -> new String[size]));						
			}
		}
	}

	/**
	 * Recursive on {@link #upsertDocument(String, Map)}
	 * @param key
	 * @param result
	 * @param inner
	 * @param level
	 */
	@SuppressWarnings("rawtypes")
	private void manageComplexType(String key, RecursiveReturn result, Map inner, int level, Transaction transaction) {
		try
		{
			Node child = upsertDocument(key, inner, ++level, transaction);
			result.childrenNodes.add(child);
		}catch(Exception e)
		{
			if(logDiscadEvent){
				log.info("Discard document "+inner+": "+e.getMessage());				
			}
		}
	}

	
	/* (non-Javadoc)
	 * @see org.neo4j.helpers.json.document.DocumentGrapher#deleteDocument(java.lang.String)
	 */
	@Override
	public long deleteDocument(String key) {
		DocumentRelationContext context = new DocumentRelationContext();
		context.setDocumentKey(key);
		Transaction transaction = db.beginTx();
		Set<Node> orphans = documentRelationBuilder.deleteRelations(context, transaction);
		int size = orphans.size();
		if(log.isDebugEnabled())
		{
			log.debug("Delete "+size+" orphan node");
		}
		orphans.forEach(node -> node.delete());

		//delete root node
		//TODO use specific label 
//		db.execute("MATCH (n {"+rootNodeKeyProperty+": '"+key+"'}) DELETE n");
		transaction.execute("MATCH (n {"+rootNodeKeyProperty+": '"+key+"'}) DELETE n");
		transaction.commit();
		return size;
	}
	
}
