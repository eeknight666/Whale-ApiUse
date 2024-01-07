package burp;

import java.awt.*;
import java.net.URL;
import javax.swing.*;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.StandardCharsets;


public class BurpExtender implements IBurpExtender, ITab {
    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;
    private PrintWriter stdout;
    private JPanel mainPanel;
    @Override
    public void registerExtenderCallbacks(final IBurpExtenderCallbacks callbacks) {
        this.callbacks = callbacks;
        this.helpers = callbacks.getHelpers();
        stdout = new PrintWriter(callbacks.getStdout(), true);
        this.stdout.println("hello Whale-ApiUse!!!");
        this.stdout.println("version:1.0");
        callbacks.setExtensionName("Whale-ApiUse");
        String[] Options = {"微信公众号", "微信小程序", "企业微信", "钉钉", "飞书"};
        String[] WxminiprogramOptions = {"查询域名配置", "获取性能数据", "获取访问来源", "获取客户端版本", "查询实时日志", "获取用户反馈列表", "获取 mediaId 图片", "查询js错误详情", "查询错误列表", "获取分阶段发布详情"};
        String[] WxOptions = {"获取微信服务器IP地址"};
        String[] QyWxOptions = {"域名IP段", "获取部门列表", "获取部门成员列表", "获取部门成员详情", "获取加入企业二维码", "创建成员"};
        String[] DDOptions = {"获取应用列表", "获取管理员信息", "新建账号", "删除账号", "获取角色列表", "获取账号信息"};
        String[] FlyOptions = {"获取企业信息", "新建账号", "删除账号", "获取根部门列表", "获取账号信息", "获取部门用户列表"};
        SwingUtilities.invokeLater(() -> {
            mainPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            JLabel wxAppIdLabel = createLabel("WxAppId：",0,0,1,0,1,gbc);
            JTextField wxAppIdField = createTextField(1,0,1,1,gbc);

            JLabel wxSecretLabel = createLabel("WxSecret：",0,1,1,0,1,gbc);
            JTextField wxSecretField = createTextField(1,1,1,1,gbc);
            JButton AccessTokenvalidateButton = createButton("获取AccessToken",2,1,0,1,gbc);
            JComboBox<String> comboBox = createComboBox(Options,2,0,1,0,gbc);

            JLabel accessTokenLabel = createLabel("AccessToken：",0,2,1,0,2,gbc);
            gbc.gridx = 1;
            gbc.gridy = 2;
            gbc.weightx = 1.0;
            gbc.gridheight = 2;
            gbc.insets = new Insets(0, 10, 10, 10);
            JTextArea accessTokenField = new JTextArea(2, 20); // 设置为两行高
            accessTokenField.setLineWrap(true); // 允许行包装
            accessTokenField.setWrapStyleWord(true); // 按单词包装
            accessTokenField.setEditable(false); // 设置为不可编辑
            JScrollPane scrollPaneAccesstoken = new JScrollPane(accessTokenField);
            // 设置滚动面板的首选大小
            scrollPaneAccesstoken.setPreferredSize(new Dimension(400, 50));
            mainPanel.add(scrollPaneAccesstoken, gbc);
            JComboBox<String> use_comboBox = createComboBox(WxOptions,2,2,1,0,gbc);

            JLabel ApiConfigPost = createLabel("接口配置：",0,4,1,0,2,gbc);
            JTextField ApiConfigPostField = createTextField(1,4,1,2,gbc);
            JButton AccessTokenUseButton = createButton("AccessToken利用",2,3,0,3,gbc);

            gbc.gridx = 0;
            gbc.gridy = 6; // Next row
            gbc.gridwidth = GridBagConstraints.REMAINDER; // Span the rest of the row
            gbc.weightx = 1.0;
            gbc.weighty = 1.0; // Take up the rest of the vertical space
            gbc.fill = GridBagConstraints.BOTH; // Fill both horizontally and vertically
            JTextArea textArea = new JTextArea();
            textArea.setLineWrap(true); // 允许行包装
            textArea.setWrapStyleWord(true); // 按单词包装
            textArea.setEditable(false); // 设置为不可编辑
            textArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            JScrollPane scrollPane = new JScrollPane(textArea);
            mainPanel.add(scrollPane, gbc);

            AccessTokenvalidateButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String AppId = wxAppIdField.getText().trim();
                    String Secret = wxSecretField.getText().trim();
                    new SwingWorker<String, Void>() {
                        @Override
                        protected String doInBackground() throws Exception {
                            String selectApi = (String) comboBox.getSelectedItem();
                            String GetAccessTokenUrlStr = "";
                            if ("钉钉".equals(selectApi)) {
                                GetAccessTokenUrlStr = "https://oapi.dingtalk.com/gettoken?appkey="+ AppId + "&appsecret=" + Secret;
                            }else  if ("企业微信".equals(selectApi)) {
                                GetAccessTokenUrlStr = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid="+ AppId + "&corpsecret=" + Secret;
                            }else  if ("飞书".equals(selectApi)) {
                                String   jsonData = "{"
                                        + "\"app_id\": \"" + AppId +"\","
                                        + "\"app_secret\": " + Secret
                                        + "}";
                                return  executeHttpPost("https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid="+ AppId + "&corpsecret=" + Secret,jsonData);
                            }else {
                                GetAccessTokenUrlStr = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
                                        + AppId + "&secret=" + Secret;
                            }
                            return executeHttpGet(GetAccessTokenUrlStr);
                        }

                        @Override
                        protected void done() {
                            try {
                                String response = get();
                                // Regex to extract access_token
                                Pattern pattern = Pattern.compile("\"access_token\":\"(.*?)\"");
                                Matcher matcher = pattern.matcher(response);
                                if (matcher.find()) {
                                    String accessToken = matcher.group(1);
                                    accessTokenField.setText(accessToken);
                                } else {
                                    // Regex to check for error
                                    pattern = Pattern.compile("\"errcode\":(\\d+)");
                                    matcher = pattern.matcher(response);
                                    if (matcher.find()) {
                                        accessTokenField.setText("wxAppId或wxSecret有误");
                                    } else {
                                        accessTokenField.setText("响应格式不符，无法解析AccessToken");
                                    }
                                }
                            } catch (Exception ex) {
                                stdout.println("获取AccessToken失败: " + ex.getMessage());
                                accessTokenField.setText("获取AccessToken失败。");
                            }
                        }
                    }.execute();
                }
            });
            //api利用接口调用
            AccessTokenUseButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String AccessToken = accessTokenField.getText().trim();
                    new SwingWorker<String, Void>() {
                        @Override
                        protected String doInBackground() throws Exception {
                            String selectApi = (String) comboBox.getSelectedItem();
                            String selectMethod = (String) use_comboBox.getSelectedItem();
                            String jsonData = "";
                            if ("钉钉".equals(selectApi)) {
                                if ("获取应用列表".equals(selectMethod)) {
                                    return executeHttpGet("https://oapi.dingtalk.com/chat/get?access_token=" + AccessToken);
                                }
                                else if("获取管理员信息".equals(selectMethod)) {
                                    return executeHttpGet("https://oapi.dingtalk.com/topapi/user/listadmin?access_token=" + AccessToken);
                                }
                                else if("新建账号".equals(selectMethod)) {
                                    if (ApiConfigPostField.getText().trim().isEmpty()) {
                                        jsonData = "{"
                                                + "\"userid\": \"999\","
                                                + "\"name\": \"桀桀桀\","
                                                + "\"mobile\": \"13999999999\","
                                                + "\"title\": \"开发人员\","
                                                + "\"hired_date\": \"1615219200000\","
                                                + "\"work_place\": \"未来park\","
                                                + "\"senior_mode\": \"false\","
                                                + "\"dept_id_list\": \"486882112,609916111\""
                                                + "}";
                                    }
                                    else {
                                        jsonData=ApiConfigPostField.getText().trim();
                                    }
                                    return executeHttpPost("https://oapi.dingtalk.com/topapi/v2/user/create?access_token=" + AccessToken,jsonData);
                                }
                                else if("删除账号".equals(selectMethod)) {
                                    return executeHttpGet("https://oapi.dingtalk.com/topapi/v2/user/delete?access_token=" + AccessToken + "&userid=999");
                                }
                                else if("获取角色列表".equals(selectMethod)) {
                                    return executeHttpGet("https://oapi.dingtalk.com/topapi/role/list?access_token=" + AccessToken);
                                }
                                else{
                                    return executeHttpGet("https://oapi.dingtalk.com/topapi/v2/user/get?access_token=" + AccessToken + "&userid=999");
                                }
                            }
                            else  if ("飞书".equals(selectApi)) {
                                return  executeHttpGet("https://open.feishu.cn/open-apis/contact/v3/users/find_by_department?department_id=");

                            }
                            else  if ("企业微信".equals(selectApi)) {
                                if ("域名IP段".equals(selectMethod)) {
                                    return executeHttpGet("https://qyapi.weixin.qq.com/cgi-bin/get_api_domain_ip?access_token=" + AccessToken);
                                }
                                else if("获取部门列表".equals(selectMethod)){
                                    return executeHttpGet("https://qyapi.weixin.qq.com/cgi-bin/department/list?access_token=" + AccessToken);
                                }
                                else if("获取部门成员列表".equals(selectMethod)){
                                    return executeHttpGet("https://qyapi.weixin.qq.com/cgi-bin/user/simplelist?access_token=" + AccessToken);
                                }
                                else if("获取加入企业二维码".equals(selectMethod)){
                                    return executeHttpGet("https://qyapi.weixin.qq.com/cgi-bin/corp/get_join_qrcode?access_token=" + AccessToken);
                                }
                                else if("创建成员".equals(selectMethod)){
                                    if (ApiConfigPostField.getText().trim().isEmpty()) {
                                        jsonData = "{"
                                                + "\"userid\": \"test999\","
                                                + "\"name\": \"桀桀桀\","
                                                + "\"department\": [1],"
                                                + "\"mobile\": \"13999999999\""
                                                + "}";
                                    }
                                    else {
                                        jsonData=ApiConfigPostField.getText().trim();
                                    }
                                    return executeHttpPost("https://qyapi.weixin.qq.com/cgi-bin/user/create?access_token=" + AccessToken,jsonData);
                                }
                                else{
                                    return executeHttpGet("https://qyapi.weixin.qq.com/cgi-bin/user/list?access_token=" + AccessToken);
                                }
                            }
                            else if ("微信小程序".equals(selectApi)) {
                                if ("查询域名配置".equals(selectMethod)) {
                                    return executeHttpGet("https://api.weixin.qq.com/wxa/getwxadevinfo?access_token=" + AccessToken);
                                }
                                else if("获取性能数据".equals(selectMethod)){
                                    // 默认为官方数据
                                    if (ApiConfigPostField.getText().trim().isEmpty()) {
                                        jsonData = "{"
                                                + "\"cost_time_type\": 2,"
                                                + "\"default_start_time\": 1572339403,"
                                                + "\"default_end_time\": 1574931403,"
                                                + "\"device\": \"@_all\","
                                                + "\"networktype\": \"@_all\","
                                                + "\"scene\": \"@_all\","
                                                + "\"is_download_code\": \"@_all\""
                                                + "}";
                                    }
                                    else {
                                        jsonData=ApiConfigPostField.getText().trim();
                                    }
                                    return executeHttpPost("https://api.weixin.qq.com/wxaapi/log/get_performance?access_token=" + AccessToken,jsonData);
                                }
                                else if("获取访问来源".equals(selectMethod)){
                                    return executeHttpGet("https://api.weixin.qq.com/wxaapi/log/get_scene?access_token=" + AccessToken);
                                }
                                else if("获取客户端版本".equals(selectMethod)){
                                    return executeHttpGet("https://api.weixin.qq.com/wxaapi/log/get_client_version?access_token=" + AccessToken);
                                }
                                else if("查询实时日志".equals(selectMethod)){
                                    return executeHttpGet("https://api.weixin.qq.com/wxaapi/userlog/userlog_search?access_token=" + AccessToken+"&date=20240101&begintime=20240101&endtime=20240115");
                                }
                                else if("获取用户反馈列表".equals(selectMethod)){
                                    return executeHttpGet("https://api.weixin.qq.com/wxaapi/feedback/list?access_token=" + AccessToken + "&page=1&num=10");
                                }
                                else if("获取 mediaId 图片".equals(selectMethod)){
                                    return executeHttpGet("https://api.weixin.qq.com/wxaapi/media/getfeedbackmedia?access_token=" + AccessToken + "&record_id=1&media_id=1");
                                }
                                else if("查询js错误详情".equals(selectMethod)){
                                    if (ApiConfigPostField.getText().trim().isEmpty()) {
                                        jsonData = "{"
                                            + "\"startTime\":  \"2024-01-01\","
                                            + "\"endTime\":  \"2024-01-15\","
                                            + "\"errorMsgMd5\":  \"f2fb4f8cd638466ad0e7607b01b7d0ca\","
                                            + "\"errorStackMd5\": \"795a63b70ce5755c7103611d93077603\","
                                            + "\"appVersion\": \"0\","
                                            + "\"sdkVersion\": \"0\","
                                            + "\"osName\": \"2\","
                                            + "\"clientVersion\": \"0\","
                                            + "\"openid\": \"\","
                                            + "\"offset\": 0,"
                                            + "\"limit\": 10,"
                                            + "\"desc\": \"0\","
                                            + "}";
                                    }
                                    else {
                                        jsonData=ApiConfigPostField.getText().trim();
                                    }
                                    return  executeHttpPost("https://api.weixin.qq.com/wxaapi/log/jserr_detail?access_token=" + AccessToken,jsonData);
                                }
                                else if("查询错误列表".equals(selectMethod)){
                                    if (ApiConfigPostField.getText().trim().isEmpty()) {
                                        jsonData = "{"
                                            + "\"startTime\":  \"2024-01-01\","
                                            + "\"endTime\":  \"2024-01-15\","
                                            + "\"errType\":  \"0\","
                                            + "\"appVersion\": \"0\","
                                            + "\"openid\": \"\","
                                            + "\"keyword\": \"\","
                                            + "\"orderby\": \"uv\","
                                            + "\"desc\": \"2\","
                                            + "\"offset\": 0,"
                                            + "\"limit\": 1"
                                            + "}";
                                    }
                                    else {
                                        jsonData=ApiConfigPostField.getText().trim();
                                    }
                                    return  executeHttpPost("https://api.weixin.qq.com/wxaapi/log/jserr_list?access_token=" + AccessToken,jsonData);
                                }
                                else{
                                    return  executeHttpGet("https://api.weixin.qq.com/wxa/getgrayreleaseplan?access_token=" + AccessToken + "&record_id=1&media_id=1");
                                }
                            }
                            else {
                                return executeHttpGet("https://api.weixin.qq.com/wxa/getgrayreleaseplan?access_token=" + AccessToken);
                            }
                        }
                        @Override
                        protected void done() {
                            try {
                                String response = get();
                                textArea.setText(response);
                            } catch (Exception ex) {
                                stdout.println("AccessToken利用失败: " + ex.getMessage());
                                accessTokenField.setText("AccessToken获取失败。");
                            }
                        }
                    }.execute();
                }
            });
            comboBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String selected = (String) comboBox.getSelectedItem();
                    use_comboBox.removeAllItems();
                    if ("钉钉".equals(selected)) {
                        wxAppIdLabel.setText("Appkey:");
                        wxSecretLabel.setText("AppSecret:");
                        for (String item : DDOptions) {
                            use_comboBox.addItem(item);
                        }
                    } else if ("飞书".equals(selected)) {
                        wxAppIdLabel.setText("app_id:");
                        wxSecretLabel.setText("app_secret:");
                        for (String item : FlyOptions) {
                            use_comboBox.addItem(item);
                        }
                    } else if ("企业微信".equals(selected)) {
                        wxAppIdLabel.setText("CorpId:");
                        wxSecretLabel.setText("CorpSecret:");
                        for (String item : QyWxOptions) {
                            use_comboBox.addItem(item);
                        }
                    }else{
                        wxAppIdLabel.setText("WxAppId:");
                        wxSecretLabel.setText("WxSecret:");
                        if ("微信小程序".equals(selected)) {
                            for (String item : WxminiprogramOptions) {
                                use_comboBox.addItem(item);
                            }
                        }else {
                            for (String item : WxOptions) {
                                use_comboBox.addItem(item);
                            }
                        }
                    }
                    wxAppIdField.setText(""); // 清空或设置为WxAppId的默认值
                    wxSecretField.setText("");
                    accessTokenField.setText("");
                    textArea.setText("");
                }
            });
            callbacks.addSuiteTab(BurpExtender.this);
        });
    }
    private JLabel createLabel(String text, int x, int y, int gw, int w, int gh, GridBagConstraints gbc) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = gw;
        gbc.weightx = w;
        gbc.gridheight = gh;
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        mainPanel.add(label, gbc);
        return label;
    }
    private JTextField createTextField(int x, int y, int w, int gh, GridBagConstraints gbc) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx = w;
        gbc.gridheight = gh;
        gbc.insets = new Insets(0, 10, 10, 10);
        JTextField textField = new JTextField();
        mainPanel.add(textField, gbc);
        return textField;
    }
    private JButton createButton(String text, int x, int y, int w, int gh, GridBagConstraints gbc) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx = w;
        gbc.gridheight = gh;
        gbc.insets = new Insets(0, 10, 10, 10);
        JButton button = new JButton(text);
        mainPanel.add(button, gbc);
        return button;
    }
    private JComboBox<String> createComboBox(String[] items, int x, int y, int h ,float w, GridBagConstraints gbc) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridheight = h;
        gbc.weightx = w;
        JComboBox<String> comboBox = new JComboBox<>(items);
        mainPanel.add(comboBox, gbc);
        return comboBox;
    }
    private String executeHttpGet(String urlString) {
        StringBuilder responseBuilder = new StringBuilder();
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            // Check if the request was successful
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        responseBuilder.append(responseLine.trim());
                    }
                }
            } else {
                // Handle HTTP error codes
                responseBuilder.append("{\"error\":\"HTTP error code: ").append(conn.getResponseCode()).append("\"}");
            }
        } catch (Exception ex) {
            stdout.println("An error occurred: " + ex.getMessage());
            return "{\"error\":\"" + ex.getMessage().replaceAll("\"", "\\\"") + "\"}";
        }
        return responseBuilder.toString();
    }
    private String executeHttpPost(String urlString, String jsonData) {
        StringBuilder responseBuilder = new StringBuilder();
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    responseBuilder.append(responseLine.trim());
                }
            }
        } catch (Exception ex) {
            // 在这里处理异常
            return "{\"error\":\"" + ex.getMessage() + "\"}";
        }
        return responseBuilder.toString();
    }

    @Override
    public String getTabCaption() {
        return "Whale-ApiUse";
    }

    @Override
    public Component getUiComponent() {
        return mainPanel;
    }
}