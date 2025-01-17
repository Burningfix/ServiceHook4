package jianqiang.com.activityhook1.classloder_hook;

import com.example.jianqiang.mypluginlibrary.RefInvoke;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;

/**
 * 由于应用程序使用的ClassLoader为PathClassLoader
 * 最终继承自 BaseDexClassLoader
 * 查看源码得知,这个BaseDexClassLoader加载代码根据一个叫做
 * dexElements的数组进行, 因此我们把包含代码的dex文件插入这个数组
 * 系统的classLoader就能帮助我们找到这个类
 * <p>
 * 这个类用来进行对于BaseDexClassLoader的Hook
 * 类名太长, 不要吐槽.
 *
 * @author weishu
 * @date 16/3/28
 */
public final class BaseDexClassLoaderHookHelper {

    public static void patchClassLoader(ClassLoader cl, File apkFile, File optDexFile)
            throws IllegalAccessException, NoSuchMethodException, IOException, InvocationTargetException, InstantiationException, NoSuchFieldException {
        // 获取 BaseDexClassLoader : pathList
        Object pathListObj = RefInvoke.getFieldObject(DexClassLoader.class.getSuperclass(), cl, "pathList");

        // 获取 PathList: Element[] dexElements
        Object[] dexElements = (Object[]) RefInvoke.getFieldObject(pathListObj, "dexElements");

        // Element 类型
        Class<?> elementClass = dexElements.getClass().getComponentType();

        // 创建一个数组, 用来替换原始的数组
        Object[] newElements = (Object[]) Array.newInstance(elementClass, dexElements.length + 1);

        // support android 8
        Object[] toAddElementArray = null;
        if (android.os.Build.VERSION.SDK_INT <= 25) {
            // 构造插件Element(File file, boolean isDirectory, File zip, DexFile dexFile) 这个构造函数
            Class[] p1 = {File.class, boolean.class, File.class, DexFile.class};
            Object[] v1 = {apkFile, false, apkFile, DexFile.loadDex(apkFile.getCanonicalPath(), optDexFile.getAbsolutePath(), 0)};
            Object o = RefInvoke.createObject(elementClass, p1, v1);

            toAddElementArray = new Object[]{o};
        } else {
            List<File> legalFiles = new ArrayList<File>();
            legalFiles.add(apkFile);

            List<IOException> suppressedExceptions = new ArrayList<IOException>();

            Class[] p1 = {List.class, File.class, List.class, ClassLoader.class};
            Object[] v1 = {legalFiles, optDexFile, suppressedExceptions, cl};
            // 拦截 dalvik.system.DexPathList.makeDexElements
            //private static Element[] makeDexElements(List<File> files, File optimizedDirectory, List<IOException> suppressedExceptions, ClassLoader loader)
            toAddElementArray = (Object[])
                    RefInvoke.invokeStaticMethod("dalvik.system.DexPathList", "makeDexElements", p1, v1);
        }

        // 把原始的elements复制进去
        System.arraycopy(dexElements, 0, newElements, 0, dexElements.length);
        // 插件的那个element复制进去
        System.arraycopy(toAddElementArray, 0, newElements, dexElements.length, toAddElementArray.length);

        // 替换
        RefInvoke.setFieldObject(pathListObj, "dexElements", newElements);
    }
}
