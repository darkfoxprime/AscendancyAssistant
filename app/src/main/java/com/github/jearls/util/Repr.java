package com.github.jearls.util;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Johnson on 2016-08-30.
 */
public class Repr {
    public static String strRepr(String str) {
        return "\"" + (str.replaceAll("([\\\\\"])", "\\$1").replace("\n", "\\n").replace("\r", "\\r").replace("\b", "\\b").replace("\t", "\\t").replace("\f", "\\f")) + "\"";
    }

    public static String arrayRepr(Object[] ary, Set<RecursivelyRepresentable> alreadyRepresented) {
        StringBuilder buf = new StringBuilder(ary.getClass().getClass().getName() + "[]");
        if (alreadyRepresented.contains(ary)) {
            buf.append("@").append(ary.toString());
        } else {
            buf.append("{");
            for (int i = 0; i < ary.length; i += 1) {
                if (i > 0) {
                    buf.append(", ");
                }
                buf.append(repr(ary[i], alreadyRepresented));
            }
            buf.append("}");
        }
        return buf.toString();
    }

    public static String repr(Object foo, Set<RecursivelyRepresentable> alreadyRepresented) {
        if (foo == null) {
            return "null";
        }
        if (foo instanceof String) {
            return strRepr((String) foo);
        }
        if (foo instanceof Object[]) {
            return arrayRepr((Object[]) foo, alreadyRepresented);
        }
        if (foo instanceof RecursivelyRepresentable) {
            return ((RecursivelyRepresentable) foo).repr(alreadyRepresented);
        }
        if (foo instanceof Representable) {
            return ((Representable) foo).repr();
        }
        return foo.toString();
    }

    public static String repr(Object foo) {
        return repr(foo, new HashSet<RecursivelyRepresentable>());
    }

    public interface Representable {
        String repr();
    }

    public interface RecursivelyRepresentable {
        String repr(Set<RecursivelyRepresentable> alreadyRepresented);
    }
}
