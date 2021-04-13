package pl.dogesoulseller.thegg;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class SpringContext implements ApplicationContextAware {

	private static ApplicationContext context;

	public static <T> T getBean(Class<T> beanClass) {
		return context.getBean(beanClass);
	}

	@Override
	public void setApplicationContext(@Nullable ApplicationContext context) throws BeansException {
		SpringContext.context = context;
	}
}