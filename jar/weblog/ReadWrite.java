package com.nanbei.weblog;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ReadWrite {
    static String readFileName;
    static String writeFileName;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("请提供读入文件路径和写入文件路径！");
            return;
        }

        // 获取输入和输出文件路径
        readFileName = args[0].trim();
        writeFileName = args[1].trim();

        // 打印路径检查
        System.out.println("输入文件路径：" + readFileName);
        System.out.println("输出文件路径：" + writeFileName);

        // 检查输出文件路径合法性
        File outputFile = new File(writeFileName);
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean dirCreated = parentDir.mkdirs();
            if (!dirCreated) {
                System.err.println("无法创建输出目录：" + parentDir);
                return;
            }
        }

        try {
            readFileByLines(readFileName);
        } catch (FileNotFoundException e) {
            System.err.println("文件未找到：" + readFileName);
        } catch (IOException e) {
            System.err.println("文件读取失败：" + e.getMessage());
        }
    }

    public static void readFileByLines(String fileName) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "GBK"))) {
            System.out.println("以行为单位读取文件内容：");
            String tempString;
            int count = 0;

            while ((tempString = br.readLine()) != null) {
                count++;
                System.out.println("row: " + count + " >>>>>>>> " + tempString);
                appendMethodA(writeFileName, tempString);
                Thread.sleep(300);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("线程被中断", e);
        }
    }

    public static void appendMethodA(String file, String content) throws IOException {
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
            out.write(content);
            out.newLine();
        }
    }
}
