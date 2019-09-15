package com.github.imkrmgn.javadocdecorator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavadocContentAccess;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

/**
 * Java要素をJavadocの1行目のテキストで装飾する。
 *
 * @author imkrmgn
 * @since 2019-09-15
 */
public class JavadocDecorator implements ILabelDecorator {

    @Override
    public Image decorateImage(Image image, Object element) {
        return null;
    }

    /* (非 Javadoc)
     * @see org.eclipse.jface.viewers.ILabelDecorator#decorateText(java.lang.String, java.lang.Object)
     */
    @Override
    public String decorateText(String text, Object element) {
        IMember member = getMember(element);
        if (member != null) {
            String line = getJavadocFirstLine(member);
            if (line != null) {
                return text + " " + line;
            }
        }
        return text;
    }

    /**
     * element から有効な IMember のインスタンスを取得する。
     * @param element
     * @return 有効なインスタンスを取得できなかった場合 null
     */
    private IMember getMember(Object element) {
        if (element instanceof ICompilationUnit) {

            ICompilationUnit compUnit = (ICompilationUnit) element;
            IType[] types;
            try {
                types = compUnit.getTypes();
            } catch (JavaModelException e) {
                return null;
            }
            if (types.length == 0) {
                return null;
            }
            return types[0];

        } else if (element instanceof IMember) {

            return (IMember) element;

        } else {

            return null;
        }
    }

    /**
     * member からJavadocの1行目を取得する。
     * @param member
     * @return 取得できなかった場合 null
     */
    private String getJavadocFirstLine(IMember member) {
        try {
            ISourceRange javadocRange = member.getJavadocRange();
            if (javadocRange == null) {
                return null;
            }
            Reader reader = JavadocContentAccess.getContentReader(member, false);
            if (reader == null) {
                return null;
            }
            BufferedReader bufReader = new BufferedReader(reader);
            String line;
            do {
                line = bufReader.readLine();
                if (line == null) {
                    return null;
                }
                line = line.trim();
                if (line.startsWith("@")) {
                    return null;
                }
            } while (line.isEmpty());
            return line;
        } catch (JavaModelException | IOException e) {
            return null;
        }
    }

    @Override
    public void addListener(ILabelProviderListener listener) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
    }
}
