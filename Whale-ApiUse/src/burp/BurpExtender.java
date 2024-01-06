package burp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        String[] Options = {"微信公众号", "微信小程序", "企业微信", "钉钉"};
        String[] WxOptions = {"获取微信服务器IP地址"};
        String[] QyWxOptions = {"域名IP段", "获取部门列表", "获取部门成员列表", "获取部门成员详情"};
        String[] DDOptions = {"获取应用列表", "获取管理员信息", "新建账号", "删除账号", "获取角色列表", "获取账号信息"};
        // Initialize the UI components
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
            gbc.weightx = 1.0;
            gbc.weighty = 1.0; // Take up the rest of the vertical space
            gbc.gridwidth = GridBagConstraints.REMAINDER; // Span the rest of the row
            gbc.fill = GridBagConstraints.BOTH; // Fill both horizontally and vertically
            JTextArea textArea = new JTextArea();
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
                    String AccessToken = accessTokenLabel.getText().trim();
                    new SwingWorker<String, Void>() {
                        @Override
                        protected String doInBackground() throws Exception {
                            String selectApi = (String) comboBox.getSelectedItem();
                            String selectMethod = (String) use_comboBox.getSelectedItem();
                            String urlStr = "";
                            if ("钉钉".equals(selectApi)) {
                                if ("获取应用列表".equals(selectMethod)) {
                                    urlStr = "" + AccessToken;
                                }
                                else{
                                    urlStr = "" + AccessToken;
                                }
                            }else  if ("企业微信".equals(selectApi)) {
                                if ("域名IP段".equals(selectMethod)) {
                                    urlStr = "https://qyapi.weixin.qq.com/cgi-bin/get_api_domain_ip?access_token=" + AccessToken;
                                }
                                else if("获取部门列表".equals(selectMethod)){
                                    urlStr = "https://qyapi.weixin.qq.com/cgi-bin/department/list?access_token=" + AccessToken;
                                }
                                else if("获取部门成员列表".equals(selectMethod)){
                                    urlStr = "https://qyapi.weixin.qq.com/cgi-bin/user/simplelist?access_token=" + AccessToken;
                                }
                                else{
                                    urlStr = "https://qyapi.weixin.qq.com/cgi-bin/user/list?access_token=" + AccessToken;
                                }
                            }else {
                                urlStr = "https://api.weixin.qq.com/cgi-bin/get_api_domain_ip?access_token=" + AccessToken;
                            }
                            return executeHttpGet(urlStr);
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
                        for (String item : QyWxOptions) {
                            use_comboBox.addItem(item);
                        }
                    } else if ("企业微信".equals(selected)) {
                        wxAppIdLabel.setText("CorpId:");
                        wxSecretLabel.setText("CorpSecret:");
                        for (String item : QyWxOptions) {
                            use_comboBox.addItem(item);
                        }
                    } else{
                        wxAppIdLabel.setText("WxAppId:");
                        wxSecretLabel.setText("WxSecret:");
                        for (String item : WxOptions) {
                            use_comboBox.addItem(item);
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
        String response = "Error";
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            // Check if the request was successful
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Scanner scanner = new Scanner(url.openStream());
                response = scanner.useDelimiter("\\Z").next();
                scanner.close();
            }
        } catch (Exception ex) {
            stdout.println("An error occurred: " + ex.getMessage());
        }
        return response;
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