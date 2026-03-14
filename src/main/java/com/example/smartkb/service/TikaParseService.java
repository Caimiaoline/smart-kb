package com.example.smartkb.service;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

/**
 * 使用 Apache Tika 解析各类文档为纯文本。
 */
@Service
public class TikaParseService {

    private final Tika tika = new Tika();

    public String parseToText(InputStream inputStream, String fileName) throws IOException, TikaException {
        return tika.parseToString(inputStream);
    }

    public String parseToText(InputStream inputStream) throws IOException, TikaException {
        return tika.parseToString(inputStream);
    }
}
