package util;

import java.io.*;

public class FileUtil {
	public static void saveImage(String root, String fname, byte[] data) throws IOException{
		root += "/images";
		File f = new File(root);
		if(!f.exists()) f.mkdir(); //이미지 파일 생성 코드 (이미지 파일이 없으면 만들어라 util 패키지의 file 클래스에 있는 함수들임)
		
		f = new File(root+ "/"+fname);
		FileOutputStream out = new FileOutputStream(f);
		out.write(data);
		out.close();
	}
}
