package rs.retrieval.tree.nona;

import java.awt.Point;

public class NonaTreeNode {
	public boolean bLeafNode = false;		//是否为叶子节点
	public int nLayer = 0;		//节点所在层数
	public int nNumber = 0;	//该层的第几个节点
	public Point ptPos = null;		//节点在原始图像中的位置
	public int nSizeWidth = 0;		//节点大小
	public NonaTreeNode[] pChildren = new NonaTreeNode[9];		//9个子节点
	
	/**
	 * 创建Nona-tree
	 * @param pNode 节点
	 * @param pData 图像数据
	 * @param nTotalWidth 节点所表示的图像宽度/高度
	 * @param nWidth 节点所表示的图像宽度/高度
	 * @param nFloorValue 叶子节点的最小尺寸
	 * @param fThreshold 相似性阈值
	 */
	public void buildNonaTree(NonaTreeNode pNode, byte[] pData, int nTotalWidth, 
			int nWidth, int nFloorValue, float fThreshold){
		if(pNode != null && !pNode.bLeafNode ){
			if(nWidth <= nFloorValue)
				pNode.bLeafNode = true;			
		}
	}
	
	/**
	 * 基于Nona-tree分块组织的图像数据特征提取
	 * @param parentFea 父节点的特征
	 * @param pData 图像数据
	 * @param nTotalWidth 原始图像的宽度/高度
	 * @param pNode 节点对象
	 * @param nWidth 节点所表示的图像宽度/高度
	 * @param childFea 子节点的特征
	 */
	public void getFeature(ImageFeature parentFea, byte[] pData, int nTotalWidth,
			NonaTreeNode pNode, int nWidth, ImageFeature[] childFea){
		
	}	
}
