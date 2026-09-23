package uptc.structures.decision;
import uptc.model.Preferencia;
@FunctionalInterface
public interface ReglaPreferencia { boolean cumple(Preferencia preferencia); }
