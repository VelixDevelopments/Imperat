package dev.velix.imperat.help;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.exception.ImperatException;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.BiConsumer;

final class HelpTemplateImpl<S extends Source> extends HelpTemplate<S> {
	
	private final HelpHyphen<S> headerProvider, footerProvider;
	private final @Nullable BiConsumer<ExecutionContext<S>, Collection<? extends CommandUsage<S>>> displayerFunc;
	
	HelpTemplateImpl(
		UsageFormatter formatter,
		HelpHyphen<S> headerProvider,
		HelpHyphen<S> footerProvider,
		@Nullable BiConsumer<ExecutionContext<S>, Collection<? extends CommandUsage<S>>> displayerFunc
	) {
		super(formatter);
		this.headerProvider = headerProvider;
		this.footerProvider = footerProvider;
		this.displayerFunc = displayerFunc;
	}
	
	@Override
	public String getHeader(Command<S> command, int currentPage, int maxPages) {
		return headerProvider.value(HyphenContent.of(command, currentPage, maxPages));
	}
	
	@Override
	public String getFooter(Command<S> command, int currentPage, int maxPages) {
		return footerProvider.value(HyphenContent.of(command, currentPage, maxPages));
	}
	
	@Override
	public void displayHeaderHyphen(Command<S> command, S source, int page) {
		source.reply(getHeader(command, 1, 1));
	}
	
	@Override
	public void displayFooterHyphen(Command<S> command, S source, int page) {
		source.reply(getHeader(command, 1, 1));
	}
	
	
	@Override
	public void display(ExecutionContext<S> context, UsageFormatter formatter, Collection<? extends CommandUsage<S>> commandUsages) throws ImperatException {
		if (displayerFunc == null)
			super.display(context, formatter, commandUsages);
		else
			displayerFunc.accept(context, commandUsages);
	}
}
