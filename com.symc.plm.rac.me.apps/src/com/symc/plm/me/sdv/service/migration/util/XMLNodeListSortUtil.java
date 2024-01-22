package com.symc.plm.me.sdv.service.migration.util;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLNodeListSortUtil {

	private boolean isAscendingOrder = true;
	private XPath xpath;
	
	public XMLNodeListSortUtil(){
		// xpath ����
		this.xpath = XPathFactory.newInstance().newXPath();
	}
	
	private NodeList doSort(NodeList srcNodeList, boolean isAscendingOrder, String keyXPathExpression) throws Exception{

		// --------------------------------------------------------
		// Sort������ �ʿ��� Sort ������ NodeList�� ���������
		// XML Document ��ü�� �����Ѵ�.
		// --------------------------------------------------------
		Element rootElement = null;
		Document xmlDoc = null;
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			if(docBuilder!=null){
				xmlDoc = docBuilder.newDocument();
				if(xmlDoc!=null){
					rootElement = xmlDoc.createElement("SearchResult");
					xmlDoc.appendChild(rootElement);
				}
			}
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}

		if(rootElement==null){
			throw new Exception("Failed to create a \"temporary NodeList\" for the alignment.");
		}
		
		// -------------------------------------
		// SrcNodeList�� �״�� ����ϴ� ��� Sort�ϴ� ������ Swap������ Parent Node�� �ҷ��ö� ������ �߻��ǹǷ�
		// �Ʒ��� ���� Argument�� ���� NodeList�� ������ Node List�� �����Ѵ�.
		// -------------------------------------
		for (int i = 0; srcNodeList!=null && i < srcNodeList.getLength(); i++) {
			Node currentNode = srcNodeList.item(i);
			
			// �ٸ� XML Document���� ������ Node�� �߰��Ҷ��� ImportNode�� �̿��Ѵ�.
			Node tempNode = xmlDoc.importNode(currentNode, true);
			// Import�ؼ� ������ Node�� rootElement�� �߰��Ѵ�.
			rootElement.appendChild(tempNode);
		}
		NodeList newNodeList = rootElement.getChildNodes();
		
		// -------------------------------------
		// ������ Node List�� ���� �ʿ��� ó���� ���� �Ѵ�.
		// -------------------------------------
		
		this.isAscendingOrder = isAscendingOrder;
		int nodeListSize = newNodeList.getLength();

		for(int sortingTurnIndex=(nodeListSize-1); sortingTurnIndex>0; sortingTurnIndex--) {
			
			for(int nodeIndex=0; nodeIndex<sortingTurnIndex; nodeIndex++) {

				Node currentNode = newNodeList.item(nodeIndex);
				Node nextNode = newNodeList.item(nodeIndex+1);
						
				if(nextNode==null || currentNode==null){
					continue;
				}

				boolean isSwapTarget = isSwapTarget(currentNode, nextNode, keyXPathExpression);
				if(isSwapTarget==true) {
					boolean deep = true;
					
					Node tempCurrentNode = currentNode.cloneNode(deep);
					Node tempNextNode = nextNode.cloneNode(deep);
					
					nextNode.getParentNode().removeChild(nextNode);
					if(tempNextNode!=null && currentNode!=null){
						currentNode.getParentNode().insertBefore(tempNextNode, currentNode);
					}
				}

			}      
			
		}
		
		// Sort�� Node List�� Return �Ѵ�.
		return newNodeList;
	}
	
	/**
	 * Node List�� Sort �ϱ����� ��ġ�� �ٲ�ߵ� ������� ������ ����� Return �Ѵ�.
	 * @param currentNode
	 * @param nextNode
	 * @param keyXPathExpression Node�� �� Key String�� �������µ� ���� XPath �˻� ����
	 * @return
	 */
	private boolean isSwapTarget(Node currentNode, Node nextNode, String keyXPathExpression){
		
		String currentValueStr = null;
		String nextValueStr = null;
		
		// XPATH�� �̿��� Attribute�� �д´�.
		try {
			currentValueStr = xpath.evaluate(keyXPathExpression, currentNode);
			nextValueStr = xpath.evaluate(keyXPathExpression, nextNode);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		
		int compareResult = 0; 
		if(currentValueStr!=null && nextValueStr!=null){
			compareResult = currentValueStr.compareToIgnoreCase(nextValueStr);
		}else if(currentValueStr==null && nextValueStr!=null){
			compareResult = 1; 
		}else if(currentValueStr!=null && nextValueStr==null){
			compareResult = -1;
		}else{
			compareResult = 0; 
		}
		
		boolean isSwaptarget = false;
		
		if(this.isAscendingOrder==true){
			if(compareResult>0){
				 isSwaptarget = true;
			}			
		}else{
			if(compareResult<0){
				 isSwaptarget = true;
			}
		}
		
		return isSwaptarget;
	}
	
	/**
	 * �־��� NodeList�� Data�� �־��� XPath �˻���� ���� �������� Sort�� ��� NodeList�� Return �Ѵ�.
	 * 
	 * @param nodeList �Է°��� �Ǵ� NodeList ��ü
	 * @param isAscendingOrder �������� ������ ��� True, �������� �����̸� False
	 * @param keyXPathExpression XPath�� �̿��� XML Element�� Attribute ���̳� Text Node�� ���� �����ϴµ� ���� Key���� ã�� XPath���� ����.
	 * @return Sort�� NodeList�� Return �Ѵ�.
	 * @throws Exception
	 */
	public static NodeList sortNodeList(NodeList nodeList, boolean isAscendingOrder, String keyXPathExpression) throws Exception{
		XMLNodeListSortUtil xmlNodeListSortUtil = new XMLNodeListSortUtil();
		return xmlNodeListSortUtil.doSort(nodeList, isAscendingOrder, keyXPathExpression);
	}

}
