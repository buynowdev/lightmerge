package cn.zynworld.lightmerge.helper;

import cn.zynworld.lightmerge.common.Result;
import cn.zynworld.lightmerge.config.SafeConfig;
import cn.zynworld.lightmerge.domain.GitBranch;
import cn.zynworld.lightmerge.domain.GitProject;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.extern.log4j.Log4j2;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.util.FS;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Log4j2
public class GitHelper {

    final static String PROJECT_WORKSPACE = "workspace/";



    public static Git cloneFromRemote(String gitAddress,String privateKeyPosition,String projectName) {
        try {
            log.info("begin . gitAddress:{} privateKeyPosition:{}  projectName:{}", gitAddress, privateKeyPosition, projectName);
            final File workspace = new File(PROJECT_WORKSPACE + projectName + "/");

            Git result = Git.cloneRepository()
                    .setURI(gitAddress)
                    .setDirectory(workspace)
                    .setTransportConfigCallback(createTransportConfigCallback(privateKeyPosition))
                    .call();

            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static void closeGit(Git git) {
        if (Objects.nonNull(git)) {
            git.close();
        }
    }

    public static Git getGitFromLocal(String projectName) {
        try {
            log.info("begin. projectName:{}", projectName);
            File gitFiles = new File(PROJECT_WORKSPACE + projectName);
            return Git.open(gitFiles);
        } catch (IOException e) {
            log.info("");
            return null;
        }
    }

    public static TransportConfigCallback createTransportConfigCallback(SafeConfig safeConfig) {
        return createTransportConfigCallback(safeConfig.getPrivateKeyPosition());
    }

    public static TransportConfigCallback createTransportConfigCallback(String privateKeyPosition) {
        JschConfigSessionFactory sessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
                session.setConfig("StrictHostKeyChecking", "no");
            }

            @Override
            protected JSch getJSch(OpenSshConfig.Host hc, FS fs) throws JSchException {
                JSch jsch = new JSch();
                jsch.removeAllIdentity();
                jsch.addIdentity(privateKeyPosition);
                return jsch;
            }
        };

        TransportConfigCallback transportConfigCallback = new TransportConfigCallback() {
            @Override
            public void configure(Transport transport) {
                SshTransport sshTransport = (SshTransport)transport;
                sshTransport.setSshSessionFactory(sessionFactory);
            }
        };

        return transportConfigCallback;
    }

    public static void main(String[] args) throws IOException {
    }

}
