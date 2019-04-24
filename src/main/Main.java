/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import com.interbanco.util.SecurityUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;

/**
 *
 * @author CARLOGON
 */
public class Main {

    public static void main(String[] args) {
        try {
            KettleEnvironment.init();
            String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            File parent = new File(path).getParentFile();
            File[] listFiles = parent.listFiles();
            String separator = System.getProperty("file.separator");
            String jobName = "";
            for (File file : listFiles) {
                if (file.getName().endsWith(".kjb")) {
                    jobName = file.getName();
                    break;
                }
            }
            if ("".equals(jobName)) {
                throw new Exception("No se encontró Job para ejecutar en el directorio raíz.");
            } else {
                String job_file = parent.getPath() + separator + jobName;
                Properties properties = new Properties();
                InputStream fileProperties = new FileInputStream(parent.getPath() + separator + "config" + separator + "config.properties");
                properties.load(fileProperties);
                JobMeta jobMeta = new JobMeta(job_file, null);
                Job job = new Job(null, jobMeta);
                for (String prop : properties.stringPropertyNames()) {
                    if (prop.startsWith("parameter")) {
                        if (prop.contains("encrypt")) {
                            String parameter = prop.replaceFirst("parameter.encrypt.", "");
                            String value = SecurityUtil.decrypt(properties.getProperty(prop));
                            job.setParameterValue(parameter, value);
                        } else {
                            String parameter = prop.replaceFirst("parameter.", "");
                            job.setParameterValue(parameter, properties.getProperty(prop));
                        }
                    } else if (prop.startsWith("variable")) {
                        if (prop.contains("encrypt")) {
                            String variable = prop.replaceFirst("variable.encrypt.", "");
                            String value = SecurityUtil.decrypt(properties.getProperty(prop));
                            job.setVariable(variable, value);
                        } else {
                            String variable = prop.replaceFirst("variable.", "");
                            job.setVariable(variable, properties.getProperty(prop));
                        }
                    }
                }
                if (args.length > 0) {
                    for (String var : args) {
                        String varName = var.split("=")[0];
                        String varValue = var.split("=")[1];
                        job.setVariable(varName, varValue);
                    }
                }
//                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
//                Date date = new Date();
//                String fecha = dateFormat.format(date);
//                job.setVariable("fecha", fecha);
                job.start();
            }
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
