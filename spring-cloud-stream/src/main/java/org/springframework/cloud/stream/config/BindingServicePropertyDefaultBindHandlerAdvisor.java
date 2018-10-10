package org.springframework.cloud.stream.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationPropertiesBindHandlerAdvisor;
import org.springframework.boot.context.properties.bind.AbstractBindHandler;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName.Form;

class BindingServicePropertyDefaultBindHandlerAdvisor implements ConfigurationPropertiesBindHandlerAdvisor {

	@Override
	public BindHandler apply(BindHandler bindHandler) {
		return new PropertyDefaultsBindHandler(bindHandler);
	}

	private static class PropertyDefaultsBindHandler extends AbstractBindHandler {

		private final Map<ConfigurationPropertyName, ConfigurationPropertyName> mappings;

		PropertyDefaultsBindHandler(BindHandler bindHandler) {
			super(bindHandler);
			this.mappings = new LinkedHashMap<>();
			this.mappings.put(ConfigurationPropertyName.of("spring.cloud.stream.bindings"),
					ConfigurationPropertyName.of("spring.cloud.stream.default"));
		}

		@Override
		public <T> Bindable<T> onStart(ConfigurationPropertyName name, Bindable<T> target,
				BindContext context) {
			ConfigurationPropertyName defaultName = getDefaultName(name);
			if (defaultName != null) {
				BindResult<T> result = context.getBinder().bind(defaultName, target);
				if (result.isBound()) {
					return target.withExistingValue(result.get());
				}
			}
			return super.onStart(name, target, context);

		}

		private ConfigurationPropertyName getDefaultName(ConfigurationPropertyName name) {
			for (Map.Entry<ConfigurationPropertyName, ConfigurationPropertyName> mapping : this.mappings
					.entrySet()) {
				ConfigurationPropertyName from = mapping.getKey();
				ConfigurationPropertyName to = mapping.getValue();
				if (from.isAncestorOf(name) && name.getNumberOfElements() > from.getNumberOfElements()) {
					ConfigurationPropertyName defaultName = to;
					for (int i = from.getNumberOfElements() + 1; i < name.getNumberOfElements(); i++) {
						defaultName = defaultName.append(name.getElement(i, Form.UNIFORM));
					}
					return defaultName;
				}
			}
			return null;
		}

	}

}
