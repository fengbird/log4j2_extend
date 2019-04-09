package com.maeeki.log4j2.elk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maeeki.log4j2.util.IpUtil;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.layout.PatternSelector;
import org.apache.logging.log4j.core.pattern.RegexReplacement;

import java.io.File;
import java.net.SocketException;
import java.nio.charset.Charset;

/**
 * ELK日志格式
 * @Author lnk
 * @Date 2018/3/13
 */
@Plugin(name = "ElkJsonPatternLayout", category = Node.CATEGORY, elementType = Layout.ELEMENT_TYPE, printObject = true)
public class ElkJsonPatternLayout extends AbstractStringLayout {
    private PatternLayout patternLayout;
    private String projectName;
    private String logType;
    private String ip;

    private ElkJsonPatternLayout(Configuration config, RegexReplacement replace, String eventPattern,
                                 PatternSelector patternSelector, Charset charset, boolean alwaysWriteExceptions,
                                 boolean noConsoleNoAnsi, String headerPattern, String footerPattern, String projectName,
                                 String logType,String ip) {
        super(config, charset,
                PatternLayout.createSerializer(config, replace, headerPattern, null, patternSelector, alwaysWriteExceptions,
                        noConsoleNoAnsi),
                PatternLayout.createSerializer(config, replace, footerPattern, null, patternSelector, alwaysWriteExceptions,
                        noConsoleNoAnsi));

        this.projectName = projectName;
        this.logType = logType;
        if ("%ip".equals(ip)) {
            try {
                ip = IpUtil.getIpAddress().getHostAddress();
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
        this.ip = ip;
        this.patternLayout = PatternLayout.newBuilder()
                .withPattern(eventPattern)
                .withPatternSelector(patternSelector)
                .withConfiguration(config)
                .withRegexReplacement(replace)
                .withCharset(charset)
                .withAlwaysWriteExceptions(alwaysWriteExceptions)
                .withNoConsoleNoAnsi(noConsoleNoAnsi)
                .withHeader(headerPattern)
                .withFooter(footerPattern)
                .build();
    }

    @Override
    public String toSerializable(LogEvent event) {
        //在这里处理日志内容
        String message = patternLayout.toSerializable(event);
        String jsonStr = new JsonLoggerInfo(projectName, message, event.getLevel().name(), logType,ip, event.getTimeMillis()).toString();
        return jsonStr + "\n";
    }

    @PluginFactory
    public static ElkJsonPatternLayout createLayout(
            @PluginAttribute(value = "pattern", defaultString = PatternLayout.DEFAULT_CONVERSION_PATTERN) final String pattern,
            @PluginElement("PatternSelector") final PatternSelector patternSelector,
            @PluginConfiguration final Configuration config,
            @PluginElement("Replace") final RegexReplacement replace,
            @PluginAttribute(value = "charset") final Charset charset,
            @PluginAttribute(value = "alwaysWriteExceptions", defaultBoolean = true) final boolean alwaysWriteExceptions,
            @PluginAttribute(value = "noConsoleNoAnsi", defaultBoolean = false) final boolean noConsoleNoAnsi,
            @PluginAttribute("header") final String headerPattern,
            @PluginAttribute("footer") final String footerPattern,
            @PluginAttribute("projectName") final String projectName,
            @PluginAttribute("logType") final String logType,
            @PluginAttribute("ip") final String ip) {


        return new ElkJsonPatternLayout(config, replace, pattern, patternSelector, charset,
                alwaysWriteExceptions, noConsoleNoAnsi, headerPattern, footerPattern, projectName, logType,ip);
    }

    /**
     * 输出的日志内容
     */
    public static class JsonLoggerInfo{
        /** 项目名 */
        private String projectName;
        /** 日志信息 */
        private String message;
        /** 日志级别 */
        private String level;
        /** 日志分类 */
        private String logType;
        private String ip;
        /** 日志时间 */
        private String time;

        public JsonLoggerInfo(String projectName, String message, String level, String logType,String ip, long timeMillis) {
            this.projectName = projectName;
            this.message = message;
            this.level = level;
            this.logType = logType;
            this.ip = ip;
            this.time = DateFormatUtils.format(timeMillis, "yyyy-MM-dd HH:mm:ss.SSS");
        }

        public String getIp() {
            return ip;
        }

        public String getProjectName() {
            return projectName;
        }

        public String getMessage() {
            return message;
        }

        public String getLevel() {
            return level;
        }

        public String getLogType() {
            return logType;
        }

        public String getTime() {
            return time;
        }

        @Override
        public String toString() {
            try {
                return new ObjectMapper().writeValueAsString(this);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
