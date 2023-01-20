package com.jarvis.platform.filestorage.sdk.impl;

import com.jarvis.framework.core.exception.BusinessException;
import com.jarvis.platform.filestorage.sdk.FileStorageSdkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * @author qiucs
 * @version 1.0.0 2022年7月11日
 */
@Slf4j
public class LocalStorageSdkServiceImpl implements FileStorageSdkService {

    /**
     * @see com.gdda.archives.platform.filestorage.sdk.FileStorageSdkService#makeLocation(java.lang.String)
     */
    @Override
    public void makeLocation(String location) {
        final File loc = new File(location);
        if (!loc.exists()) {
            loc.mkdirs();
        }
    }

    /**
     * @see com.gdda.archives.platform.filestorage.sdk.FileStorageSdkService#makedir(java.lang.String, java.lang.String)
     */
    @Override
    public void makedir(String location, String dir) {
        final File loc = new File(location, dir);
        if (!loc.exists()) {
            loc.mkdirs();
        }
    }

    /**
     * @see com.gdda.archives.platform.filestorage.sdk.FileStorageSdkService#upload(java.lang.String, java.lang.String,
     * java.lang.String)
     */
    @Override
    public boolean upload(String location, String path, String filename) {
        try {
            FileCopyUtils.copy(new File(filename), new File(location, path));
            return true;
        } catch (final IOException e) {
            throw new BusinessException(String.format("上传本地文件[%s]出错", filename), e);
        }
    }

    /**
     * @see com.gdda.archives.platform.filestorage.sdk.FileStorageSdkService#upload(java.lang.String, java.lang.String,
     * java.io.File)
     */
    @Override
    public boolean upload(String location, String path, File file) {
        try {
            FileCopyUtils.copy(file, new File(location, path));
            return true;
        } catch (final IOException e) {
            throw new BusinessException(String.format("上传本地文件[%s]出错", file.getAbsolutePath()), e);
        }
    }

    /**
     * @see com.gdda.archives.platform.filestorage.sdk.FileStorageSdkService#upload(java.lang.String, java.lang.String,
     * java.io.InputStream)
     */
    @Override
    public boolean upload(String location, String path, InputStream is) {
        try {
            final OutputStream os = new FileOutputStream(new File(location, path));
            FileCopyUtils.copy(is, os);
            return true;
        } catch (final IOException e) {
            throw new BusinessException(String.format("上传本地文件出错"), e);
        }
    }

    /**
     * @see com.gdda.archives.platform.filestorage.sdk.FileStorageSdkService#download(java.lang.String,
     * java.lang.String, long, long, java.io.OutputStream)
     */
    @Override
    public void download(String location, String path, OutputStream os) {
        InputStream fis = null;
        try {
            fis = new FileInputStream(toFile(location, path));
            StreamUtils.copy(fis, os);
        } catch (final Exception e) {
            throw new BusinessException(String.format("下载本地文件出错"), e);
        } finally {
            if (null != fis) {
                try {
                    fis.close();
                } catch (final IOException e) {
                    log.error("关闭文件流出错", e);
                }
                fis = null;
            }
        }
    }

    /**
     * @see com.gdda.archives.platform.filestorage.sdk.FileStorageSdkService#download(java.lang.String,
     * java.lang.String, long, long, java.io.OutputStream)
     */
    @Override
    public void download(String location, String path, long start, long end, OutputStream os) {
        InputStream fis = null;
        try {

            fis = new FileInputStream(toFile(location, path));

            if (start == -1 && end == -1) {
                StreamUtils.copy(fis, os);
                return;
            }

            if (start < 0) {
                throw new BusinessException(String.format("下载本地文件开始位置[%d]不能小于0", start));
            }
            if (end < start) {
                throw new BusinessException(String.format("下载本地文件结束位置[%d]不能小于开始位置[%d]", end, start));
            }

            StreamUtils.copyRange(fis, os, start, end);
        } catch (final Exception e) {
            throw new BusinessException(String.format("下载本地文件出错"), e);
        } finally {
            if (null != fis) {
                try {
                    fis.close();
                } catch (final IOException e) {
                    log.error("关闭文件流出错", e);
                }
                fis = null;
            }
        }
    }

    /**
     * @see com.gdda.archives.platform.filestorage.sdk.FileStorageSdkService#download(java.lang.String,
     * java.lang.String)
     */
    @Override
    public InputStream download(String location, String path) {
        try {
            return new FileInputStream(toFile(location, path));
        } catch (final Exception e) {
            throw new BusinessException(String.format("下载本地文件出错"), e);
        }
    }

    /**
     * @see com.gdda.archives.platform.filestorage.sdk.FileStorageSdkService#download(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public File download(String location, String path, String downloadFilePath) {
        final File downloadFile = new File(downloadFilePath);
        try {
            if (!downloadFile.getParentFile().exists()) {
                downloadFile.getParentFile().mkdirs();
            }
            FileCopyUtils.copy(toFile(location, path), downloadFile);
        } catch (final IOException e) {
            throw new BusinessException(String.format("下载本地文件出错"), e);
        }
        return downloadFile;
    }

    /**
     * @see com.gdda.archives.platform.filestorage.sdk.FileStorageSdkService#delete(java.lang.String,
     * java.lang.String[])
     */
    @Override
    public void delete(String location, String... paths) {
        Stream.of(paths).forEach(p -> FileSystemUtils.deleteRecursively(toFile(location, p)));
    }

    /**
     * @see com.gdda.archives.platform.filestorage.sdk.FileStorageSdkService#copy(java.lang.String, java.lang.String,
     * java.lang.String)
     */
    @Override
    public void copy(String location, String path, String toPath) {
        copy(location, path, location, toPath);
    }

    /**
     * @see com.gdda.archives.platform.filestorage.sdk.FileStorageSdkService#copy(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void copy(String location, String path, String toLocation, String toPath) {
        try {
            final File sourceFile = toFile(location, path);
            if (!sourceFile.exists()) {
                throw new BusinessException(String.format("复制本地文件[%s]不存在", sourceFile.getAbsolutePath()));
            }

            final File toFile = toFile(toLocation, toPath);
            if (!toFile.getParentFile().exists()) {
                toFile.getParentFile().mkdirs();
            }
            FileCopyUtils.copy(sourceFile, toFile);
        } catch (final IOException e) {
            throw new BusinessException(String.format("复制本地文件出错"), e);
        }
    }

    /**
     * @see com.gdda.archives.platform.filestorage.sdk.FileStorageSdkService#move(java.lang.String, java.lang.String,
     * java.lang.String)
     */
    @Override
    public void move(String location, String path, String toPath) {
        copy(location, path, toPath);
        delete(location, path);

    }

    /**
     * @see com.gdda.archives.platform.filestorage.sdk.FileStorageSdkService#move(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void move(String location, String path, String toLocation, String toPath) {
        copy(location, path, toLocation, toPath);
        delete(location, path);
    }

    /**
     * @see com.gdda.archives.platform.filestorage.sdk.FileStorageSdkService#merge(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String, java.util.Comparator)
     */
    @Override
    public void merge(String location, String dir, String toLocation, String toPath, Comparator<String> comparator) {
        final File dirFile = toFile(location, dir);

        if (!dirFile.exists()) {
            throw new BusinessException(String.format("合并的目录[%s]不存在", dirFile.getAbsolutePath()));
        }
        final File[] listFiles = dirFile.listFiles((p, name) -> new File(p, name).isFile());
        if (0 == listFiles.length) {
            throw new BusinessException(String.format("合并的目录[%s]下没有文件", dirFile.getAbsolutePath()));
        }

        final File toFile = toFile(toLocation, toPath);
        if (toFile.exists()) {
            if (log.isWarnEnabled()) {
                log.warn("删除已存在的合并后文件[{}]再进行合并", toFile.getAbsolutePath());
            }
            toFile.delete();
        }
        if (!toFile.getParentFile().exists()) {
            toFile.getParentFile().mkdirs();
        }
        final Path path = toFile.toPath();
        try {
            Files.createFile(path);
        } catch (final IOException e) {
            throw new BusinessException(String.format("合并本地文件出错"), e);
        }

        Stream.of(listFiles).map(f -> f.getName()).sorted(comparator).forEach(f -> {
            try {
                Files.write(path, Files.readAllBytes(new File(dirFile, f).toPath()), StandardOpenOption.APPEND);
            } catch (final IOException e) {
                throw new BusinessException(String.format("合并本地文件出错"), e);
            }
        });

        for (final File f : listFiles) {
            f.delete();
        }

        if (0 == dirFile.list().length) {
            dirFile.delete();
        }
    }

    private File toFile(String location, String path) {
        return new File(location, path);
    }

}
