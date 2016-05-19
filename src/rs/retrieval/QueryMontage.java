package rs.retrieval;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import javax.imageio.ImageIO;

public class QueryMontage {
	int zoom = 12;
	String sPath = "D:\\GeoData\\TianDiTu\\BeiJing\\Temp\\";
	
	/**
	 * 接接经纬度范围内的图像块，失败返回NULL
	 * @param sqadLatLon
	 * @return 以经纬度保存的文件名（经度_纬度.jpg）
	 */
	public String montagePatch(double[] latLon){
		double sqadLatLon[] = new double[latLon.length];
		double lenEdge = Math.max(Math.abs(latLon[0]-latLon[2]), Math.abs(latLon[1]-latLon[3]));
		sqadLatLon[0] = latLon[0];
		sqadLatLon[1] = latLon[1];
		sqadLatLon[2] = latLon[0] + lenEdge;
		sqadLatLon[3] = latLon[1] - lenEdge;
		
		//计算中心点的经纬度（用于标注和保存名称）
		double centerLon = (sqadLatLon[0] + sqadLatLon[2])/2; 
		double centerLat = (sqadLatLon[1] + sqadLatLon[3])/2;
		String resultImgName = String.format("%.6f", centerLon).replace('.', '-');
		resultImgName += "_" + String.format("%.6f", centerLat).replace('.', '-');
		resultImgName += ".jpg";
				
		//计算经纬度范围内的瓦片块
		double[] tileNum = getTilesNumber(sqadLatLon);
		
		int left = (int) Math.floor(tileNum[0]);
		int top = (int) Math.floor(tileNum[1]);
		int right = (int) Math.ceil(tileNum[2]);
		int bottom = (int) Math.ceil(tileNum[3]);
		
		Random rnd = new Random((new Date()).getTime());
		int server = rnd.nextInt() % 8;
		int rows = Math.abs(bottom - top);
		int cols = Math.abs(right - left);
		//下载图片
		String[] tileNames = new String[rows * cols];
		int iURL = 0;
		for (int i = Math.min(left, right); i < Math.max(left, right); i++) {
			for (int j = Math.min(top, bottom); j < Math.max(top, bottom); j++) {
				tileNames[iURL] = getTileName(i, j, zoom);
				String tileURL = getTileURL(i, j, zoom, server);
				int counts = 0;
				while(!downloadFile(tileURL, tileNames[iURL])){		//轮询可访问的URL
					server = rnd.nextInt() % 8;
					tileURL = getTileURL(i, j, zoom, server);
					if(counts++ > 10)
						return null;
				}
				iURL++;
			}
		}
		//拼接图片
		String montageImgPath = sPath + UUID.randomUUID().toString() + ".jpg";
		ImageOperation.montage(rows, cols, sPath, tileNames, montageImgPath);
		
		//剪切图片，得到区域图片
		int startX = (int) Math.floor((tileNum[0] - left) * 256.00);
		int startY = (int) Math.floor((tileNum[1] - top) * 256.00);
		int endX = (int) Math.ceil((right - tileNum[2]) * 256.00);
		int endY = (int) Math.ceil((bottom - tileNum[3]) * 256.00);
		int width = (int) Math.abs(right-left) * 256 - startX - endX;
		int height = (int) Math.abs(bottom - top) * 256 - startY - endY;
		
		String subPath = sPath + resultImgName;
		ImageOperation.cut(montageImgPath, startX, startY, width, height, subPath);
		
		return resultImgName;
	}
	
	/**
	 * 获取范围内的瓦片所有行列号
	 * @param latLon 经纬度范围
	 * @return 行列号[left top right bottom]
	 */
	private double[] getTilesNumber(double[] latLon) {
		//int[] tileNum = new int[4];
		
		double lat = latLon[0]; // Left
		double lon = latLon[1]; // Top
		double left = (lat + 180.00) * Math.pow(2, zoom) / 360.00;
		double top = (90.00 - lon) * Math.pow(2, (zoom - 1)) / 180.00;
		//tileNum[0] = (int) Math.floor(dX);	//Left
		//tileNum[1] = (int) Math.floor(dY);	//Top

		lat = latLon[2]; // Right
		lon = latLon[3]; // Bottom
		double right = (lat + 180.00) * Math.pow(2, zoom) / 360.00;
		double bottom = (90.00 - lon) * Math.pow(2, (zoom - 1)) / 180.00;
		//tileNum[2] = (int) Math.floor(dX);	//Right
		//tileNum[3] = (int) Math.floor(dY);	//Bottom
		double[] tileNum = new double[]{left, top, right, bottom};
		
		return tileNum;
	}
	/**
	 * 获取瓦片的URL
	 * @param x 列号
	 * @param y 行号
	 * @param zoom 缩放级别
	 * @param server 服务器
	 * @return 瓦片的URL
	 */
	private String getTileURL(int x, int y, int zoom, int server) {
		// http://t0.tianditu.cn/img_c/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=img&STYLE=default&TILEMATRIXSET=c&TILEMATRIX=11&TILEROW=282&TILECOL=1684&FORMAT=tiles
		String preURL = "http://t" + Math.abs(server) + ".tianditu.cn/img_c/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=img&STYLE=default&TILEMATRIXSET=c";
		return preURL + "&TILEMATRIX=" + Integer.toString(zoom) + "&TILEROW=" + Integer.toString(y) + "&TILECOL=" + Integer.toString(x) + "&FORMAT=tiles";
	}
	/**
	 * 获取瓦片对应的名称
	 * @param x 列号
	 * @param y 行号
	 * @param zoom 缩放级别
	 * @return 瓦片文件名称
	 */
	private String getTileName(int x, int y, int zoom){
		return Integer.toString(y) + "_" + Integer.toString(x) + "_" + Integer.toString(zoom) + ".jpg";
	}
	/**
	 * 下载URL
	 * @param sURL 文件的URL
	 * @param sName 文件存储的名称
	 * @return 下载是否成功
	 */
	private boolean downloadFile(String sURL, String sName) {
		File fTile = new File(sPath + sName);
		if (fTile.exists()) {
			return true;
		}

		int nStartPos = 0;
		int nRead = 0;
		try {
			URL url = new URL(sURL);
			// 打开连接
			HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
			// int state = httpConnection.getResponseCode();
			// if(state != 200)
			// return false;
			// 获得文件长度
			long nEndPos = getFileSize(sURL);
			if (nEndPos < 0 || nEndPos == 2407)
				return false;

			RandomAccessFile oSavedFile = new RandomAccessFile(sPath + sName, "rw");
			httpConnection.setRequestProperty("User-Agent", "Internet Explorer");
			String sProperty = "bytes=" + nStartPos + "-";
			// 告诉服务器book.rar这个文件从nStartPos字节开始传
			httpConnection.setRequestProperty("RANGE", sProperty);
			// System.out.println(sProperty);
			InputStream input = httpConnection.getInputStream();
			byte[] b = new byte[1024];
			// 读取网络文件,写入指定的文件中
			while ((nRead = input.read(b, 0, 1024)) > 0 && nStartPos < nEndPos) {
				oSavedFile.write(b, 0, nRead);
				nStartPos += nRead;
			}
			oSavedFile.close();
			httpConnection.disconnect();
		} catch (Exception e) {
			//e.printStackTrace();
			return false;
		}
		return true;
	}

	/** 
	 * 获得文件长度
	 * @param sURL 文件的URL
	 * @return 文件的长度
	 */
	private long getFileSize(String sURL) {
		int nFileLength = -1;
		try {
			URL url = new URL(sURL);
			HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.setRequestProperty("User-Agent", "Internet Explorer");

			int responseCode = httpConnection.getResponseCode();
			if (responseCode >= 400) {
				return -2; // -2 represent access is error
			}
			String sHeader;
			for (int i = 1;; i++) {
				sHeader = httpConnection.getHeaderFieldKey(i);
				if (sHeader != null) {
					if (sHeader.equals("Content-Length")) {
						nFileLength = Integer.parseInt(httpConnection.getHeaderField(sHeader));
						break;
					}
				} else
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// System.out.println(nFileLength);
		return nFileLength;
	}
	
	public void montage(int rows, int cols, String[] names, String monPath) throws IOException {
		int chunks = rows * cols;

		int chunkWidth, chunkHeight;
		int type;

		// 读入小图
		File[] imgFiles = new File[chunks];
		for (int i = 0; i < chunks; i++) {
			imgFiles[i] = new File(sPath + names[i]);
		}

		// 创建BufferedImage
		BufferedImage[] buffImages = new BufferedImage[chunks];
		for (int i = 0; i < chunks; i++) {
			buffImages[i] = ImageIO.read( imgFiles[i]);
		}
		type = buffImages[0].getType();
		chunkWidth = buffImages[0].getWidth();
		chunkHeight = buffImages[0].getHeight();

		// 设置拼接后图的大小和类型
		BufferedImage finalImg = new BufferedImage(chunkWidth * cols, chunkHeight * rows, type);

		// 写入图像内容
		int num = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				finalImg.createGraphics().drawImage(buffImages[num], chunkWidth * j, chunkHeight * i, null);
				num++;
			}
		}
		// 输出拼接后的图像
		ImageIO.write(finalImg, "BMP", new File(monPath));
		System.out.println("完成拼接！");
	}
	
	/**
	 * zoom级别下的每个像素的经度值
	 * @param zoom 缩放级别
	 * @return 像素的经度值
	 */
	private double dx(int zoom) {
		return 360.00 / Math.pow(2, zoom) / 256;
	}
	/**
	 * zoom级别下的每个像素的纬度值
	 * @param zoom 缩放级别
	 * @return 像素的纬度值
	 */
	private double dy(int zoom) {
		return 180 / Math.pow(2, zoom - 1) / 256;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		QueryMontage montage = new QueryMontage();
		montage.montagePatch(new double[]{116.30, 39.94, 116.50, 39.90});
	}

}
