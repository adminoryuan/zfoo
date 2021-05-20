/*
 * Copyright (C) 2020 The zfoo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.protocol.serializer.js;

import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.registration.field.ListField;
import com.zfoo.protocol.serializer.GenerateUtils;
import com.zfoo.protocol.util.StringUtils;

import java.lang.reflect.Field;

import static com.zfoo.protocol.util.FileUtils.LS;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class JsListSerializer implements IJsSerializer {

    @Override
    public void writeObject(StringBuilder builder, String objectStr, int deep, Field field, IFieldRegistration fieldRegistration) {
        ListField listField = (ListField) fieldRegistration;

        GenerateUtils.addTab(builder, deep);
        builder.append(StringUtils.format("if ({} === null) {", objectStr)).append(LS);
        GenerateUtils.addTab(builder, deep + 1);
        builder.append("byteBuffer.writeInt(0);").append(LS);
        GenerateUtils.addTab(builder, deep);

        builder.append("} else {").append(LS);
        GenerateUtils.addTab(builder, deep + 1);
        builder.append(StringUtils.format("byteBuffer.writeInt({}.length);", objectStr)).append(LS);

        String element = "element" + GenerateUtils.index.getAndIncrement();
        GenerateUtils.addTab(builder, deep + 1);
        builder.append(StringUtils.format("{}.forEach({} => {", objectStr, element)).append(LS);
        GenerateJsUtils.jsSerializer(listField.getListElementRegistration().serializer())
                .writeObject(builder, element, deep + 2, field, listField.getListElementRegistration());
        GenerateUtils.addTab(builder, deep + 1);
        builder.append("});").append(LS);
        GenerateUtils.addTab(builder, deep);
        builder.append("}").append(LS);
    }

    @Override
    public String readObject(StringBuilder builder, int deep, Field field, IFieldRegistration fieldRegistration) {
        ListField listField = (ListField) fieldRegistration;
        String result = "result" + GenerateUtils.index.getAndIncrement();

        GenerateUtils.addTab(builder, deep);
        builder.append(StringUtils.format("const {} = [];", result)).append(LS);

        GenerateUtils.addTab(builder, deep);
        String size = "size" + GenerateUtils.index.getAndIncrement();
        builder.append(StringUtils.format("const {} = byteBuffer.readInt();", size)).append(LS);

        GenerateUtils.addTab(builder, deep);
        builder.append(StringUtils.format("if ({} > 0) {", size)).append(LS);

        GenerateUtils.addTab(builder, deep + 1);
        String i = "index" + GenerateUtils.index.getAndIncrement();
        builder.append(StringUtils.format("for (let {} = 0; {} < {}; {}++) {", i, i, size, i)).append(LS);
        String readObject = GenerateJsUtils.jsSerializer(listField.getListElementRegistration().serializer())
                .readObject(builder, deep + 2, field, listField.getListElementRegistration());
        GenerateUtils.addTab(builder, deep + 2);
        builder.append(StringUtils.format("{}.push({});", result, readObject)).append(LS);
        GenerateUtils.addTab(builder, deep + 1);
        builder.append("}").append(LS);
        GenerateUtils.addTab(builder, deep);
        builder.append("}").append(LS);


        return result;
    }
}
