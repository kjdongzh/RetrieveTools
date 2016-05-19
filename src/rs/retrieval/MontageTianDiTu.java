package rs.retrieval;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

public class MontageTianDiTu {
	
	private String tilePath = "D:\\GeoData\\TianDiTu\\BeiJing\\15\\";
	private String montagePath = "D:\\GeoData\\TianDiTu\\BeiJing\\Montage\\";
	private String montageName = "BeiJing_15_1.bmp";
	
	public void montageRowTiles() throws IOException{
		File file = new File(tilePath);
		String tileNames[] = file.list();
		Map<String, List<Integer> > rowIndexs = new HashMap<String, List<Integer> >();
		
		for(int i=0; i<tileNames.length; i++){
			//System.out.println(tileNames[i]);
			String ss[] = tileNames[i].split("_");
			if(!rowIndexs.containsKey(ss[0])){
				rowIndexs.put(ss[0], new ArrayList<Integer>());
			}
			rowIndexs.get(ss[0]).add(i);
		}
		
		for(String row:rowIndexs.keySet()){
			List<Integer> rowTiles = rowIndexs.get(row);
			String rowNames[] = new String[rowTiles.size()];
			for(int i=0; i<rowTiles.size(); i++){
				rowNames[i] = tileNames[rowTiles.get(i)];
			}
			montage(1, rowTiles.size(), tilePath, rowNames, montagePath + row + ".jpg");
		}
	}
	
	public void montageColTiles() throws IOException{
		File file = new File(montagePath);
		String tileNames[] = file.list();
		
		for(int i=0; i<tileNames.length; i++){
			montage(tileNames.length, 1, montagePath, tileNames, montagePath + montageName);
		}
	}

	public void montage(int rows, int cols, String tPath, String[] names, String monPath) throws IOException {
		//int rows = 2;
		//int cols = 2;
		int chunks = rows * cols;

		int chunkWidth, chunkHeight;
		int type;

		File[] imgFiles = new File[chunks];
		for (int i = 0; i < chunks; i++) {
			//imgFiles[i] = new File("C:\\img\\merge\\img" + i + ".jpg");
			imgFiles[i] = new File(tPath + names[i]);
		}

		BufferedImage[] buffImages = new BufferedImage[chunks];
		for (int i = 0; i < chunks; i++) {
			buffImages[i] = ImageIO.read( imgFiles[i]);
		}
		type = buffImages[0].getType();
		chunkWidth = buffImages[0].getWidth();
		chunkHeight = buffImages[0].getHeight();
		
		BufferedImage finalImg = new BufferedImage(chunkWidth * cols, chunkHeight * rows, type);

		int num = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				finalImg.createGraphics().drawImage(buffImages[num], chunkWidth * j, chunkHeight * i, null);
				num++;
			}
		}
		ImageIO.write(finalImg, "JPG", new File(monPath));
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		MontageTianDiTu mon = new MontageTianDiTu();
		mon.montageRowTiles();
		mon.montageColTiles();
	}

}
