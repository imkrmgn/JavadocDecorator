/*-******************************************************************************
 * (c) 2019 Yoshiyuki Takemori.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *    Yoshiyuki Takemori
 *******************************************************************************/
package com.github.imkrmgn.javadocdecorator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavadocContentAccess;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

/**
 * Java要素をJavadocの見出し（1行目）または戻り値の説明で装飾する。
 *
 * @author Yoshiyuki Takemori
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
        if (member == null) {
            return null;
        }
        String line = getDecoration(member);
        if (line == null) {
            return null;
        }
        return text + " " + line;
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
     * member からJavadocの見出し（1行目）または戻り値の説明を取得する。
     * @param member
     * @return 見出しまたは戻り値の説明。取得できなかった場合 null
     */
    private String getDecoration(IMember member) {
        try (BufferedReader bufReader = getJavadocReader(member)) {

            String firstLine = getJavadocFirstLine(bufReader);

            if (isHeadingLine(firstLine)) {
                return firstLine;
            }
            if (member instanceof IMethod) {
                return getReturnTagValue(firstLine, bufReader);
            } else {
                return null;
            }
        } catch (JavaModelException | IOException e) {
            return null;
        }
    }

    private BufferedReader getJavadocReader(IMember member) throws JavaModelException {
        ISourceRange javadocRange = member.getJavadocRange();
        if (javadocRange == null) {
            return null;
        }
        Reader reader = JavadocContentAccess.getContentReader(member, false);
        if (reader == null) {
            return null;
        }
        return new BufferedReader(reader);
    }

    /**
     * @return 1行目
     */
    private String getJavadocFirstLine(BufferedReader bufReader) throws IOException {
        for (String line = bufReader.readLine();
                line != null;
                line = bufReader.readLine()) {
            line = line.trim();
            if (!line.isEmpty()) {
                return line;
            }
        };
        return null;
    }

    /**
     * @param firstLine 1行目
     * @return 見出し行なら true
     */
    private boolean isHeadingLine(String firstLine) {
        return firstLine != null && !firstLine.startsWith("@");
    }

    /**
     * @param firstLine 1行目
     * @param bufReader
     * @return 戻り値の説明
     * @throws IOException
     */
    private String getReturnTagValue( String firstLine, BufferedReader bufReader) throws IOException {
        for (String line = firstLine;
                line != null;
                line = bufReader.readLine()) {
            line = line.trim();
            if (line.startsWith("@return")) {
                final String returnTagValue = line.replaceFirst("^@return\\s+", "");
                return returnTagValue.isEmpty() ? null : returnTagValue;
            }
        }
        return null;
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
