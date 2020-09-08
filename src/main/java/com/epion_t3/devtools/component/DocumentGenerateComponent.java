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
import java.util.Map;

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
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache m = mf.compile("function.mustache");
        for (Map.Entry<String, FunctionModel> entry : context.getFunctionModelMap().entrySet()) {
            try (StringWriter sw = new StringWriter()) {
                m.execute(sw, entry.getValue());
                sw.flush();
                FileUtils.write(new File(context.getExecuteOptions().getDocOutput(),
                        context.getSpec().getInfo().getName() + "_spec.md"), sw.toString(), "UTF-8");
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
