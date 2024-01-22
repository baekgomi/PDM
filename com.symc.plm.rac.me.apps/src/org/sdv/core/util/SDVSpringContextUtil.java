/**
 * 
 */
package org.sdv.core.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;

import common.Logger;

/**
 * Class Name : SpringContextUtil
 * Class Description :
 * 
 * @date 2013. 9. 24.
 * 
 */
public class SDVSpringContextUtil {

	private static final Logger									logger							  	= Logger.getLogger(SDVSpringContextUtil.class);

	protected static final String								DEFAULT_SDV_CORE_CONTEXT_PATH	   	= "classpath*:/org/sdv/core/context/*.xml";
	protected static final int									DEFAULT_SDV_REFRESCH_CHECK_INTERVAL = 10;

	protected static Map<String, SDVClassPathXmlApplicationContext> ctxMap							= new HashMap<String, SDVClassPathXmlApplicationContext>();
	protected static boolean									supportBeanNoRefresh				= false;
	protected static double										beanRefreshInterval				 	= DEFAULT_SDV_REFRESCH_CHECK_INTERVAL;

	protected static ApplicationContextMonitor					ctxMonitor;

	// ���ҽ� ������ �ѹ� ���� �� ���ҽ� ������ �߰����� �ʴ��� ���� ������ �ٽ� �о�������� �ʴµ�, ������ �߰��� ��� �� ���� true�� �����Ͽ� ������Ʈ �� �� �ִ�.
	public static boolean										forceResourceFileRefresh			= false;

	static {
		ctxMonitor = new ApplicationContextMonitor();

		try {
			supportBeanNoRefresh = Boolean.parseBoolean(System.getProperty("SDV_BEAN_NO_REFRESH"));
		} catch (Exception ex) {
			logger.debug(ex);
		}

		try {
			beanRefreshInterval = Double.valueOf(System.getProperty("SDV_BEAN_REFRESH_INTERVAL", String.valueOf(DEFAULT_SDV_REFRESCH_CHECK_INTERVAL)));
		} catch (Exception ex) {
			logger.debug(ex);
		}
	}

	public static SDVClassPathXmlApplicationContext getContext(String contextPath) {

		SDVClassPathXmlApplicationContext ctx = null;
		if (StringUtils.isEmpty(contextPath))
			contextPath = DEFAULT_SDV_CORE_CONTEXT_PATH;

		if (ctxMap.containsKey(contextPath)) {
			ctx = ctxMap.get(contextPath);
		} else {
			ctx = new SDVClassPathXmlApplicationContext(contextPath);
			logger.info(ctx.getBeanFactory().toString());
			ctxMap.put(contextPath, ctx);
		}
		
		try{
			//���� �ʱ�ȭ�� �Ǿ� ���� �ʰų� �������� �� ��� IllegalStateException ������ �߻��Ѵ�.
    		ctx.getBeanFactory();
    		
    		//������ ���ٸ� ���� �������� ���θ� üũ�Ѵ�.
    		if (!supportBeanNoRefresh) {
    			return ctxMonitor.checkReresh(ctx);
    		}
		}catch(IllegalStateException iex){
			//������ �߻��ϸ� Context�� ���Ӱ� �ε��Ͽ� �ش�.
			ctx.refresh();
		}
		return ctx;		
	}

	public static Object getBean(String beanId) {
		return getBean(beanId, (String) null);
	}

	public static Object getBean(String beanId, String context) {
		return getContext(context).getBean(beanId);
	}

	public static Object getBean(String beanId, Object[] args) {
		return getBean(beanId, null, args);
	}

	public static Object getBean(String beanId, String context, Object[] args) {
		return getContext(context).getBean(beanId, args);
	}

	protected static class ApplicationContextMonitor {

		private boolean isInitialized = false;
		long			lastTime;
		long			startTime;

		public ApplicationContextMonitor() {

		}

		SDVClassPathXmlApplicationContext checkReresh(SDVClassPathXmlApplicationContext ctx) {

			if (!isInitialized) {
				lastTime = startTime = ctx.getStartupDate();
				isInitialized = true;
			}

			if (forceResourceFileRefresh || lastTime < System.currentTimeMillis() - (beanRefreshInterval * 60 * 1000)) {
				lastTime = System.currentTimeMillis();
				logger.info("Spring Last Check Date = " + new Date(startTime) + "\nCuurent Check Date = " + new Date(lastTime));

				try {
					List<File> resourceFiles = ctx.getResourceFiles(forceResourceFileRefresh);
					if (resourceFiles != null) {
						for(File resourceFile : resourceFiles) {
							try {
								long lastModified = resourceFile.lastModified();
								logger.info(ctx.getDisplayName() + " Last Modified =" + new Date(lastModified));
								logger.info(ctx.getDisplayName() + " Location =" + resourceFile.getName());

								if (lastModified > startTime) {
									startTime = lastTime;
									ctx.refreshContext();
									return ctx;
								}
							} catch (Exception ex) {
								logger.debug(ex);
							}
						}
					}
				} catch (IOException iex) {
					logger.error(iex);
				} catch (Exception ex) {
					logger.error(ex);
				}
			}
			return ctx;
		}
	}

	protected static class SDVClassPathXmlApplicationContext extends ClassPathXmlApplicationContext {

		private List<File> resourceFiles;

		public SDVClassPathXmlApplicationContext(String contextPath) {
			super(contextPath);
			setClassLoader(SDVClassPathXmlApplicationContext.class.getClassLoader());
		}

		public String[] getConfigAllLocations() {
			return getConfigLocations();
		}

		public Resource[] getConfigAllResources() {
			return getConfigResources();
		}

		public void refreshContext() {
			refresh();
		}

		public List<File> getResourceFiles(boolean forceRefresh) throws IOException {
			if (forceRefresh)
				resourceFiles = null;

			if (resourceFiles == null) {
				ConfigurableListableBeanFactory bf = getBeanFactory();
				for(String beanName : bf.getBeanDefinitionNames()) {
					AbstractBeanDefinition bd = (AbstractBeanDefinition) bf.getBeanDefinition(beanName);
					if (bd != null && bd.getResource() != null) {
						File resourceFile = bd.getResource().getFile();
						if (resourceFile == null)
							continue;

						if (resourceFiles == null)
							resourceFiles = new ArrayList<File>();
						if (resourceFiles.contains(resourceFile))
							continue;
						resourceFiles.add(resourceFile);
					}
				}
			}

			return resourceFiles;
		}

	}
}
