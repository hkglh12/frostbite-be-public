package com.uruz.frostbite.utils;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class FileUtils {
    private final Tika tika;

    public FileUtils(){
        this.tika = new Tika();
    }

    public String getFileExtension(File file) throws IOException {
        // String parameter의 경우 filename으로부터 ext를 유추. 파일을 직접 확인하려는 경우 File 이나 URI로 전달해야함
        return tika.detect(file);
    }

}
