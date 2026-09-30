package uptc.controller.tree;

import java.util.function.Predicate;

/**
 * Nodo del árbol de decisión. Un nodo interno tiene una pregunta y dos ramas
 * (sí / no); una hoja solo tiene el perfil resultante.
 */
public class DecisionNode {
    private final String question;
    private final Predicate<DecisionContext> rule;
    private final DecisionNode yes;
    private final DecisionNode no;
    private final RecommendationProfile profile;

    private DecisionNode(String question, Predicate<DecisionContext> rule, DecisionNode yes, DecisionNode no,
            RecommendationProfile profile) {
        this.question = question;
        this.rule = rule;
        this.yes = yes;
        this.no = no;
        this.profile = profile;
    }

    /** Crea un nodo interno: una pregunta con su rama "sí" y su rama "no". */
    static DecisionNode question(String text, Predicate<DecisionContext> rule, DecisionNode yes, DecisionNode no) {
        return new DecisionNode(text, rule, yes, no, null);
    }

    /** Crea una hoja con el perfil que se asigna al llegar a ella. */
    static DecisionNode leaf(RecommendationProfile profile) {
        return new DecisionNode(null, null, null, null, profile);
    }

    public boolean isLeaf() {
        return profile != null;
    }

    /** Indica si el contexto cumple la pregunta de este nodo. */
    public boolean answer(DecisionContext context) {
        return rule.test(context);
    }

    /** Devuelve la rama que corresponde a la respuesta. */
    public DecisionNode next(boolean answer) {
        return answer ? yes : no;
    }

    public String getQuestion() {
        return question;
    }

    public RecommendationProfile getProfile() {
        return profile;
    }
}
