package dev.velix.imperat;

import dev.velix.imperat.context.Source;
import dev.velix.imperat.help.HelpProvider;
import org.jetbrains.annotations.*;

public sealed interface CommandHelpHandler<S extends Source> permits ImperatConfig {


	/**
	 * @return The template for showing help
	 */
	@Nullable
	HelpProvider<S> getHelpProvider();

	/**
	 * Set the help template to use
	 *
	 * @param template the help template
	 */
	void setHelpProvider(@Nullable HelpProvider<S> template);

}
