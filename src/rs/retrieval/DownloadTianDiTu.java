package rs.retrieval;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Random;

public class DownloadTianDiTu {
	private double[] latLon = new double[4]; // Left,Top,Right,Bottom
	private static int zoom = 17;
	private int numErr = 0;

	//String sPath = "D:\\GeoData\\TianDiTu\\China\\Images\\" + Integer.toString(zoom) + "\\";
	String sPath = "D:\\GeoData\\TianDiTu\\TianJin\\Images\\" + Integer.toString(zoom) + "\\";
	
	public static void main(String[] args) {
		//double[] latLon = { 116.00, 40.30, 118.10, 38.50 };		//BeiJing
		//double[] latLon = { 73.60, 53.55, 135.04, 18.00 };	//China
		double[] latLon = { 117.30, 39.35, 117.50, 39.10 };	//TianJin
		DownloadTianDiTu download = new DownloadTianDiTu(latLon, zoom);
		try {
			int start = 0;
			int end = -1;
			if(args.length > 0)
				start = Integer.parseInt(args[0]);
			if(args.length > 1)
				start = Integer.parseInt(args[1]);
			download.downloadFiles(start, end);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public DownloadTianDiTu(double[] latLon, int zoom) {
		this.latLon = latLon;
		DownloadTianDiTu.zoom = zoom;
	}

	public void downloadFiles(int start, int end) throws IOException {
		double lat = latLon[0]; // Left
		double lon = latLon[1]; // Top
		double dX = (lat + 180.00) * Math.pow(2, zoom) / 360.00;
		double dY = (90.00 - lon) * Math.pow(2, (zoom - 1)) / 180.00;
		int left = (int) Math.floor(dX);
		int top = (int) Math.floor(dY);

		lat = latLon[2]; // Right
		lon = latLon[3]; // Bottom
		dX = (lat + 180.00) * Math.pow(2, zoom) / 360.00;
		dY = (90.00 - lon) * Math.pow(2, (zoom - 1)) / 180.00;
		int right = (int) Math.floor(dX);
		int bottom = (int) Math.floor(dY);

		int numURL = (Math.abs(right - left) + 1) * (Math.abs(top - bottom) + 1);
		System.out.println("共有图片" + numURL + "个，开始下载：");
		
		String preURL = null;
		String tileURL = null;
		String tileName = null;
		boolean bSuccess = false;
		
		File errURLs = new File("errURLs.txt");
		BufferedWriter errWriter = null;
		errWriter = new BufferedWriter(new FileWriter(errURLs));
		
		Random rnd = new Random((new Date()).getTime());
		int server = rnd.nextInt() % 8;
		int iserver = 0, iURL = 0;
		int minlr = Math.min(left, right) + start;
		int maxlr = end<0 ? Math.max(left, right) : Math.min(Math.min(left, right) + end, Math.max(left, right));
		for (int i = minlr; i <= maxlr; i++) {
			System.out.print("第" + i + "(" + (i-minlr) + ")行:");
			iURL = 0;
			for (int j = Math.min(top, bottom); j <= Math.max(top, bottom); j++) {	
				iserver = 0;
				do{
					server = rnd.nextInt() % 8;
					preURL = "http://t" + Math.abs(server) + ".tianditu.cn/img_c/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=img&STYLE=default&TILEMATRIXSET=c";
					tileURL = preURL + "&TILEMATRIX=" + Integer.toString(zoom) + "&TILEROW=" + Integer.toString(j) + "&TILECOL=" + Integer.toString(i) + "&FORMAT=tiles";
					tileName = Integer.toString(j) + "_" + Integer.toString(i) + "_" + Integer.toString(zoom) + ".jpg";
					bSuccess = downloadFile(tileURL, tileName);
					if(++iserver > 3){
						errWriter.write(tileURL);
						break;
					}
				}while(!bSuccess);
				if(iURL++ > 10){
					System.out.print(".");
					iURL = 0;
				}
			}
			System.out.println();
		}
		
		errWriter.close();
		System.out.println("下载完成!");
	}

	private boolean downloadFile(String sURL, String sName) {
		File fTile = new File(sPath + sName);
		if (fTile.exists()) {
			return true;
		}

		int nStartPos = 0;
		int nRead = 0;
		try {
			URL url = new URL(sURL);
			HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
			// int state = httpConnection.getResponseCode();
			// if(state != 200)
			// return false;

			long nEndPos = getFileSize(sURL);
			if (nEndPos < 0 || nEndPos == 2407)
				return false;

			RandomAccessFile oSavedFile = new RandomAccessFile(sPath + sName, "rw");
			httpConnection.setRequestProperty("User-Agent", "Internet Explorer");
			String sProperty = "bytes=" + nStartPos + "-";
			httpConnection.setRequestProperty("RANGE", sProperty);
			InputStream input = httpConnection.getInputStream();
			byte[] b = new byte[1024];
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

	private long getFileSize(String sURL) {
		int nFileLength = -1;
		try {
			URL url = new URL(sURL);
			HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.setRequestProperty("User-Agent", "Internet Explorer");

			int responseCode = httpConnection.getResponseCode();
			if (responseCode >= 400) {
				// System.err.println("Error Code : " + responseCode);
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
				} 
				else
					break;
			}
		} catch (IOException e) {
			//e.printStackTrace();
		} catch (Exception e) {
			//e.printStackTrace();
		}
		// System.out.println(nFileLength);
		return nFileLength;
	}

	public void getTileURL() {
		double lat = latLon[0]; // Left
		double lon = latLon[1]; // Top
		double dX = (lat + 180.00) * Math.pow(2, zoom) / 360.00;
		double dY = (90.00 - lon) * Math.pow(2, (zoom - 1)) / 180.00;
		int left = (int) Math.floor(dX);
		int top = (int) Math.floor(dY);

		lat = latLon[2]; // Right
		lon = latLon[3]; // Bottom
		dX = (lat + 180.00) * Math.pow(2, zoom) / 360.00;
		dY = (90.00 - lon) * Math.pow(2, (zoom - 1)) / 180.00;
		int right = (int) Math.floor(dX);
		int bottom = (int) Math.floor(dY);

		int numURL = (Math.abs(right - left) + 1) * (Math.abs(top - bottom) + 1);
		String tileURL[] = new String[numURL];
		String tileName[] = new String[numURL];
		String errURL[] = new String[numURL];
		String errName[] = new String[numURL];

		Random rnd = new Random((new Date()).getTime());
		int server = rnd.nextInt() % 8;
		int iURL = 0;
		for (int i = Math.min(left, right); i <= Math.max(left, right); i++) {
			for (int j = Math.min(top, bottom); j <= Math.max(top, bottom); j++) {
				// http://t0.tianditu.cn/img_c/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=img&STYLE=default&TILEMATRIXSET=c&TILEMATRIX=11&TILEROW=282&TILECOL=1684&FORMAT=tiles
				String preURL = "http://t" + Math.abs(server) + ".tianditu.cn/img_c/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=img&STYLE=default&TILEMATRIXSET=c";
				tileURL[iURL] = preURL + "&TILEMATRIX=" + Integer.toString(zoom) + "&TILEROW=" + Integer.toString(j) + "&TILECOL=" + Integer.toString(i) + "&FORMAT=tiles";
				server = rnd.nextInt() % 8;
				tileName[iURL++] = Integer.toString(j) + "_" + Integer.toString(i) + "_" + Integer.toString(zoom) + ".bmp";
				System.out.println(tileURL[iURL - 1]);
			}
		}
	}
}
