package gg.amy.utt.transform;

/**
 * @author amy
 * @since 2/27/22.
 */
public interface Transformer {
    Object transformInput(String input);

    String transformOutput(Object input);
}
