package net.bestidear.jettyinput;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.util.Log;

/**
 * 　　* 解压zip文件 　　
 */

public class UnzipFile {

	private static final int BufferSize = 1024*2;
	private static final String scriptFile="factory_update_param.aml";
	/**
	 * 
	 * @param targetPath
	 * @param zipFilePath
	 * @return 0 解压失败，1解压成功，不含升级脚本，2解压成功，含升级脚本。
	 */
	public static int unzipFile(String targetPath, String zipFilePath) {
		int res=1;
		try {
			File zipFile = new File(zipFilePath);
			InputStream is = new FileInputStream(zipFile);
			unzipFile(targetPath,is);			
		} catch (Exception e) {
			res=0;
			e.printStackTrace();
		}
		return res;
	}
	
	public static int unzipFile(String targetPath, InputStream is) {
		int res=1;
		try {			
			ZipInputStream zis = new ZipInputStream(is);
			ZipEntry entry = null;
			byte[] bytes = new byte[BufferSize];
			while ((entry = zis.getNextEntry()) != null) {
				String zipPath = entry.getName();				
				try {
					if (entry.isDirectory()) {
						File zipFolder = new File(targetPath+zipPath);
						if (!zipFolder.exists()) {
							zipFolder.mkdirs();

						}
					} else {
						File file = new File(targetPath+zipPath);
						if(file.exists()){
							file.delete();
						}						
						FileOutputStream fos = new FileOutputStream(file);
						int bread;
						while ((bread = zis.read(bytes,0,bytes.length)) != -1) {
							fos.write(bytes,0,bread);
						}
						fos.close();
					}
					System.out.println("成功解压:" + zipPath);
				} catch (Exception e) {
					Log.i("UnzipFile","解压" + zipPath + "失败");
					res=0;
					break;
				}
			}
			zis.close();
			is.close();
			System.out.println("解压结束");
//			File deleteFile = new File(zipFilePath);
//			deleteFile.delete();
		} catch (Exception e) {
			res=0;
			e.printStackTrace();
		}
		return res;
	}
	
	public boolean isUpdateFile(String zipFile)
	{
		boolean res=false;
		try {
			File file = new File(zipFile);
			InputStream is = new FileInputStream(file);
			
			ZipInputStream zis = new ZipInputStream(is);
			ZipEntry entry = null;
			while (( entry = zis.getNextEntry()) != null) {
				String zipPath = entry.getName();				
				String fileName = zipPath.substring(zipPath.indexOf("/")+1);				
				if(fileName.equalsIgnoreCase(scriptFile))
					{
					   res=true;	
					   break;
					}				
			}
			zis.close();
			is.close();
		} catch (Exception e) {
			res=false;
			e.printStackTrace();
		}
		return res;
	}
	
   public boolean deleteFile(String filePath) {
		File file = new File(filePath);
//		List<String> fileName = new ArrayList<String>(); 
		if (!file.exists())
			return false;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String filename;
			while ((filename = reader.readLine()) != null) {
				
				File deleteFile = new File(filename);
				if (deleteFile.exists())
					deleteFile.delete();
			}
			reader.close();
			file.delete();
		} catch (Exception ex) {
			return false;
		}
		return true;
	}
	
	public static void saveVersionInfo(String file, String conent) {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file, false)));
			out.write(conent);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	} 
	
	public String getVersionInfo(String filePath){
		File file = new File(filePath);
		String version = "";
		if (!file.exists())
			return version;
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			
			while ((version = reader.readLine()) != null) {
				 break;
			}
			reader.close();
			file.delete();
		} catch (Exception ex) {
			return "";
		}
		return version;
	}
	
	public static void writer(String file, String conent) {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file, true)));
			out.write(conent+"\n");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	} 
	
}
