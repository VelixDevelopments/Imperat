package dev.velix.imperat;

final class VelocityBrigadier extends BaseBrigadierManager<VelocitySource> {
    
    private VelocityBrigadier(Imperat<VelocitySource> dispatcher) {
        super(dispatcher);
    }
    
    static VelocityBrigadier create(Imperat<VelocitySource> dispatcher) {
        return new VelocityBrigadier(dispatcher);
    }
    
    @Override
    public VelocitySource wrapCommandSource(Object commandSource) {
        return dispatcher.wrapSender(commandSource);
    }
}
