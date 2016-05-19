package rs.retrieval;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public class ImageOperation {
	private static String PNG = "png";
	private static String JPG = "jpg";
	private static String BMP = "bmp";

	private String tilePath;
	private String montagePath;

	public ImageOperation(String tPath, String monPath) {
		tilePath = tPath;
		montagePath = monPath;
	}

	public void montageRowTiles(String[] tileNames) {
		Map<String, List<Integer>> rowIndexs = new HashMap<String, List<Integer>>();

		for (int i = 0; i < tileNames.length; i++) {
			String ss[] = tileNames[i].split("_");
			if (!rowIndexs.containsKey(ss[0])) {
				rowIndexs.put(ss[0], new ArrayList<Integer>());
			}
			rowIndexs.get(ss[0]).add(i);
		}

		for (String row : rowIndexs.keySet()) {
			List<Integer> rowTiles = rowIndexs.get(row);
			String rowNames[] = new String[rowTiles.size()];
			for (int i = 0; i < rowTiles.size(); i++) {
				rowNames[i] = tileNames[rowTiles.get(i)];
			}
			montage(1, rowTiles.size(), tilePath, rowNames, montagePath + row + ".bmp");
		}
	}

	public void montageColTiles(String[] tileNames, String montageName) {

		for (int i = 0; i < tileNames.length; i++) {
			montage(tileNames.length, 1, montagePath, tileNames, montagePath + montageName);
		}
	}

	/**
	 * 拼接图片	 * @param rows
	 * @param cols
	 * @param tPath
	 * @param names
	 * @param monPath
	 */
	public static void montage(int rows, int cols, String tPath, String[] names, String montagePath) {
		int chunks = rows * cols;

		int chunkWidth, chunkHeight;
		int type;

		// 读入小图
		File[] imgFiles = new File[chunks];
		for (int i = 0; i < chunks; i++) {
			imgFiles[i] = new File(tPath + names[i]);
		}

		// 创建BufferedImage
		BufferedImage[] buffImages = new BufferedImage[chunks];
		try {
			for (int i = 0; i < chunks; i++) {
				buffImages[i] = ImageIO.read(imgFiles[i]);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		type = buffImages[0].getType();
		chunkWidth = buffImages[0].getWidth();
		chunkHeight = buffImages[0].getHeight();

		// 设置拼接后图的大小和类型
		BufferedImage finalImg = new BufferedImage(chunkWidth * cols, chunkHeight * rows, type);

		// 写入图像内容
		int num = 0;
		for (int i = 0; i < cols; i++) {
			for (int j = 0; j < rows; j++) {
				finalImg.createGraphics().drawImage(buffImages[num], chunkWidth * i, chunkHeight * j, null);
				num++;
			}
		}
		// 输出拼接后的图像
		try {
			ImageIO.write(finalImg, JPG, new File(montagePath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("完成拼接！");
	}

	/**
	 * 对图片裁剪，并把裁剪完的新图片进行保存
	 * @param srcPath源图片路径
	 * @param x剪切点的x坐标
	 * @param y剪切点的y坐标
	 * @param width剪切的宽度
	 * @param height剪切的高度
	 * @param subPath 剪切图片的路径
	 */
	public static void cut(String srcPath, int x, int y, int width, int height, String subPath) {
		// 文件输入流对象 is .
		FileInputStream is = null;
		// 图片文件输入流对象 iis .
		ImageInputStream iis = null;
		try {
			// 据图片文件路径读取图片文件 .
			is = new FileInputStream(srcPath);

			/*
			 * 返回包含所有当前已注册 ImageReader 的 Iterator，这些 ImageReader 声称能够解码指定格式。
			 * 参数：formatName - 包含非正式格式名称 .（例如 "jpeg" 或 "tiff"）等 。
			 */
			Iterator<ImageReader> it = ImageIO.getImageReadersByFormatName("jpeg");
			ImageReader reader = it.next();
			// 将文件流转为图片流 .
			iis = ImageIO.createImageInputStream(is);
			/*
			 * <p>iis:读取源.true:只向前搜索 </p>.将它标记为 ‘只向前搜索’。
			 * 此设置意味着包含在输入源中的图像将只按顺序读取，可能允许 reader 避免缓存包含与以前已经读取的图像关联的数据的那些输入部分。
			 */
			reader.setInput(iis, true);
			/*
			 * <p>描述如何对流进行解码的类<p>.用于指定如何在输入时从 Java Image I/O
			 * 框架的上下文中的流转换一幅图像或一组图像。用于特定图像格式的插件 将从其 ImageReader 实现的
			 * getDefaultReadParam 方法中返回 ImageReadParam 的实例。
			 */
			ImageReadParam param = reader.getDefaultReadParam();

			/*
			 * 图片裁剪区域。Rectangle 指定了坐标空间中的一个区域，通过 Rectangle 对象
			 * 的左上顶点的坐标（x，y）、宽度和高度可以定义这个区域。
			 */
			Rectangle rect = new Rectangle(x, y, width, height);

			// 提供一个 BufferedImage，将其用作解码像素数据的目标。
			param.setSourceRegion(rect);

			/*
			 * 使用所提供的 ImageReadParam 读取通过索引 imageIndex 指定的对象，并将 它作为一个完整的
			 * BufferedImage 返回。
			 */
			BufferedImage bi = reader.read(0, param);

			// 保存新图片
			ImageIO.write(bi, "jpeg", new File(subPath));
			
			// 程序最后执行关闭流文件流和图片流 .
			if (is != null)
				is.close();
			if (iis != null)
				iis.close();
			
			System.out.println("切割成功！");
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
}
