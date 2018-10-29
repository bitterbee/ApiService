package com.netease.libs.apiservice_process.generator;

import com.netease.libs.apiservice.anno.ApiServiceClassBuildMethodAnno;
import com.netease.libs.apiservice.anno.ApiServiceConstructAnno;
import com.netease.libs.apiservice.anno.ApiServiceMethodAnno;
import com.netease.libs.apiservice_process.ApiServiceClass;
import com.netease.libs.apiservice_process.BaseClassGenerator;
import com.netease.libs.apiservice_process.ElementUtil;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Created by zyl06 on 2018/10/18.
 */

public abstract class BaseApiClassGenerator extends BaseClassGenerator {

    protected ApiServiceClass mProviderClass;
    protected TypeElement mApiTarget;
    protected String mPkgName;

    public BaseApiClassGenerator(ApiServiceClass providerClass, Messager messager, String packageName) {
        super(messager);
        mProviderClass = providerClass;
        mApiTarget = providerClass.clazz;
        mPkgName = packageName;
    }

    protected void generate(TypeSpec.Builder builder) {
        for (Element e : mApiTarget.getEnclosedElements()) {
            if (!ElementUtil.isPublic(e)) {
                continue;
            }
            if (e instanceof ExecutableElement) {
                ApiServiceConstructAnno constructAnno = e.getAnnotation(ApiServiceConstructAnno.class);
                if (constructAnno != null) {
                    String methodName = !constructAnno.alias().isEmpty() ? constructAnno.alias() : "newInstance";
                    addFactoryMethod(builder, (ExecutableElement) e, methodName);
                    continue;
                }

                ApiServiceClassBuildMethodAnno classBuildAnno = e.getAnnotation(ApiServiceClassBuildMethodAnno.class);
                if (classBuildAnno != null) {
                    if (ElementUtil.isPublic(e) && ElementUtil.isStatic(e)) {
                        String methodName = !classBuildAnno.alias().isEmpty() ? constructAnno.alias() : e.getSimpleName().toString();
                        addClassBuildMethod(builder, (ExecutableElement) e, methodName);
                    }
                    continue;
                }

                runExeElement(builder, (ExecutableElement) e);
            }
        }
    }

    private void runExeElement(TypeSpec.Builder builder, ExecutableElement e) {
        ApiServiceMethodAnno anno = e.getAnnotation(ApiServiceMethodAnno.class);
        if (anno != null && !anno.provide()) {
            return;
        }

        boolean provide = (anno != null && anno.provide()) ||
                (mProviderClass.allPublicStaticApi && ElementUtil.isStatic(e)) ||
                (mProviderClass.allPublicNormalApi && !ElementUtil.isStatic(e));
        if (provide) {
            String methodName = anno != null && !anno.alias().isEmpty() ?
                    anno.alias() : e.getSimpleName().toString();
            addMethod(builder, e, methodName);
        }
    }

    protected void addCustomFields(TypeSpec.Builder builder) {

    }

    protected void addMethod(TypeSpec.Builder builder, ExecutableElement e, String methodName) {

    }

    protected void addFactoryMethod(TypeSpec.Builder builder, ExecutableElement e, String methodName) {

    }

    protected void addClassBuildMethod(TypeSpec.Builder builder, ExecutableElement e, String methodName) {

    }

    /**
     *
     * @param e 具体真实 Element
     * @return 返回 对应 Api 类 TypeName 或元素自身的 TypeName
     */
    public TypeName tryConvertApiTypeName(Element e) {
        TypeName apiTypeName = ElementUtil.getApiServiceClassName(e);
        return apiTypeName != null ? apiTypeName : ClassName.get(e.asType());
    }

    /**
     *
     * @param type 真实类型
     * @return 返回 对应 Api 类 TypeName 或元素自身的 TypeName
     */
    public TypeName tryConvertApiTypeName(TypeMirror type) {
        TypeName apiTypeName = ElementUtil.getApiServiceClassName(type);
        return apiTypeName != null ? apiTypeName : ClassName.get(type);
    }
}