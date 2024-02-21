package com.gbt.cdms.amos.integration;

import org.springframework.beans.factory.annotation.Value;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class MFTIntegration {

	@Value("${sftp.hostname}")
	private String SFTP_HOSTNAME;

	@Value("${sftp.port}")
	private Integer SFTP_PORT;

	@Value("${sftp.username}")
	private String SFTP_USERNAME;

	@Value("${sftp.password}")
	private String SFTP_PASSWORD;

	@Value("${sftp.workingDir}")
	private String SFTP_WORKING_DIR;

	public void uploadFileToSftp(String localFile) throws JSchException, SftpException {
		System.out.println("Uploading file");
		ChannelSftp channelSftp = setupJsch();
		channelSftp.connect();
		channelSftp.put(localFile, SFTP_WORKING_DIR);
		channelSftp.exit();
	}

	private ChannelSftp setupJsch() throws JSchException {
		JSch jsch = new JSch();
		Session jschSession = jsch.getSession(SFTP_USERNAME, SFTP_HOSTNAME, SFTP_PORT);
		jschSession.setPassword(SFTP_PASSWORD);

		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		jschSession.setConfig(config);
		jschSession.connect();

		return (ChannelSftp) jschSession.openChannel("sftp");
	}
}