/* Copyright (c) 2017-2020 Nozomu Takashima. */
package com.epion_t3.devtools.component;

import com.epion_t3.devtools.bean.DevGeneratorContext;
import com.epion_t3.devtools.bean.FunctionModel;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ドキュメント生成処理.
 *
 * @author takashno
 */
public final class DocumentGenerateComponent implements Component {

    /**
     * シングルトンインスタンス.
     */
    private static DocumentGenerateComponent instance = new DocumentGenerateComponent();

    /**
     * テンプレートキャッシュ.
     */
    private static Map<String, Mustache> templateCache = new ConcurrentHashMap<>();

    static {
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache m_ja = mf.compile("function-ja_JP.mustache");
        Mustache m_en = mf.compile("function-en_US.mustache");
        templateCache.put(Locale.JAPAN.toString(), m_ja);
        templateCache.put(Locale.US.toString(), m_en);
    }

    /**
     * プライベートコンストラクタ.
     */
    private DocumentGenerateComponent() {
        // Do Nothing...
    }

    /**
     * シングルトンインスタンス取得.
     *
     * @return シングルトンインスタンス
     */
    public static DocumentGenerateComponent getInstance() {
        return instance;
    }

    /**
     * 出力処理.
     *
     * @param context コンテキスト
     */
    public void execute(DevGeneratorContext context) {

        createFunctionDocument(context);

    }

    /**
     * 機能ドキュメント出力.
     *
     * @param context コンテキスト
     */
    private void createFunctionDocument(DevGeneratorContext context) {
        for (Map.Entry<String, FunctionModel> entry : context.getFunctionModelMap().entrySet()) {
            Locale l = new Locale(entry.getKey().split("_")[0], entry.getKey().split("_")[1]);
            Mustache template = null;
            if (templateCache.containsKey(l.toString())) {
                template = templateCache.get(l.toString());
            } else {
                throw new RuntimeException("未対応のロケールです:" + l.toString());
            }
            try (StringWriter sw = new StringWriter()) {
                template.execute(sw, entry.getValue());
                sw.flush();
                FileUtils.write(
                        new File(context.getExecuteOptions().getDocOutput(),
                                context.getSpec().getInfo().getName() + "_spec_" + l.toString() + ".md"),
                        sw.toString(), "UTF-8");
            } catch (IOException e) {

            }
        }
    }

    private String createConfiguration() {
        return null;
    }

    private String createFlow() {
        return null;
    }

}
