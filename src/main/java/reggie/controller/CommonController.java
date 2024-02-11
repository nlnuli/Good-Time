package reggie.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reggie.common.R;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    /***
     * 文件上传，上传到服务器
     * @param file
     * @return
     */
    //参数名字必须与前端保持一致
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        int index = file.getOriginalFilename().lastIndexOf(".");
        String suffix = file.getOriginalFilename().substring(index);
        //UUID生成重新文件名，防止文件名重复
       String fileName =  UUID.randomUUID().toString();
       fileName = fileName + suffix;
       File dir = new File(basePath);
       if(!dir.exists()){
           dir.mkdirs();
       }
       //后缀截取；
        try {
            file.transferTo(new File(basePath + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(fileName);
    }

    /***
     * 文件下载
     * @param name response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        //输入流,输出流
        try {
            //服务器的本地文件：
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));
            ServletOutputStream outputStream = response.getOutputStream();
            int len = 0;
            byte[] bytes = new byte[1024];
            response.setContentType("image/jpeg");


            while( (len = fileInputStream.read(bytes)) != -1 ) {
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }
            outputStream.close();
            fileInputStream.close();


        } catch (Exception e) {
            e.printStackTrace();
        }

        //输出流：

    }
}
