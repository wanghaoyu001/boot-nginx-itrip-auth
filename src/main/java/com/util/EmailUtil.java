package com.util;

import com.po.ItripUser;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

public class EmailUtil {
    public static String account = "wang931476436@vip.qq.com";            //替换为发件人账号
    public static String password = "ellgnaeowsuubcfh";                    //替换为发件人账号密码
    public static String email = null;    //验证码
    public static String receiveMailAccount = "931476436@qq.com";    //替换为收件人账号

    public static String check(ItripUser user) {

        // 1. 使用Properties对象封装连接所需的信息
        Properties props = new Properties();                    // 参数配置
        props.setProperty("mail.transport.protocol", "smtp");   // 使用的协议
        props.setProperty("mail.smtp.host", "smtp.qq.com");    // 发件人的邮箱的 SMTP 服务器地址
        props.setProperty("mail.smtp.auth", "true");            // 需要请求认证
        Date date = new Date();
        Long datetime = date.getTime();
        String val = datetime + "";
        email = val.substring(val.length() - 4, val.length());
        try {
            // 2. 获取Session对象
            Session session = Session.getDefaultInstance(props);
            // 3. 封装Message对象
            MimeMessage message = createMimeMessage(session, account, receiveMailAccount, user);
            // 4. 使用Transport发送邮件
            Transport transport = session.getTransport();
            transport.connect(account, password);
            transport.sendMessage(message, message.getAllRecipients());
            // 5. 关闭连接
            transport.close();
            System.out.println("发送成功！");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return email;
    }

    public static MimeMessage createMimeMessage(Session session, String sendMail, String receiveMail, ItripUser user)
            throws Exception {

        // 1. 创建一封邮件
        MimeMessage message = new MimeMessage(session);
        // 2. From: 发件人
        message.setFrom(new InternetAddress(sendMail, "爱旅行", "UTF-8"));
        // 3. To: 收件人
        message.setRecipient(MimeMessage.RecipientType.TO,
                new InternetAddress(receiveMail, user.getUsername() + "用户", "UTF-8"));
        // 4. Subject: 邮件主题
        message.setSubject("验证邮件", "UTF-8");
        // 5. Content: 邮件正文
        message.setContent("尊敬的" + user.getUsername() + "用户你的验证码是" + email, "text/html;charset=UTF-8");
        // 6. 设置发件时间
        message.setSentDate(new Date());
        // 7. 保存设置
        message.saveChanges();
        return message;
    }


}
