/**
 * Generic framework code included with
 * <a href="http://www.amazon.com/exec/obidos/tg/detail/-/1861007841/">Expert One-On-One J2EE Design and Development</a>
 * by Rod Johnson (Wrox, 2002).
 * This code is free to use and modify.
 * Please contact <a href="mailto:rod.johnson@interface21.com">rod.johnson@interface21.com</a>
 * for commercial support.
 */

package com.interface21.context;

import java.util.Locale;

/**
 * 将由可以解析message的对象实现的接口.
 * 这可以实现message里的参数化和国际化.
 *
 * @author Rod Johnson
 */
public interface MessageSource {

    /**
     * 尝试解析message. 如果找不到message, 则返回默认message.
     *
     * @param code           要查找的code, 例如'calculator.noRateSet'. 鼓励此类用户将message
     *                       名称基于相关的完全限定类名, 从而避免冲突并确保最大程度的清晰性.
     * @param args           将填充消息中的参数的参数数组(参数在消息中看起来像"{0}", "{1,date}", "{2,time}"),
     *                       如果没有则为null.
     * @param locale         要在其中进行查找的locale
     * @param defaultMessage 查找失败时返回的字符串
     * @return 如果查找成功, 则返回已解析的消息; 否则返回作为参数传递的默认消息;
     * @see <a href="http://java.sun.com/j2se/1.3/docs/api/java/text/MessageFormat.html">java.text.MessageFormat</a>
     */
    String getMessage(String code, Object args[], String defaultMessage, Locale locale);

    /**
     * 尝试解析该message. 如果找不到message,.则视为错误.
     *
     * @param code   要查找的code, 例如'calculator.noRateSet'
     * @param args   将填充消息中的参数的参数数组(参数在消息中看起来像"{0}", "{1,date}", "{2,time}"),
     *               如果没有则为null.
     * @param locale 要在其中进行查找的locale
     * @return message
     * @throws NoSuchMessageException 在任何locale中都找不到
     * @see <a href="http://java.sun.com/j2se/1.3/docs/api/java/text/MessageFormat.html">java.text.MessageFormat</a>
     */
    String getMessage(String code, Object args[], Locale locale) throws NoSuchMessageException;

    /**
     * <b>使用传入的<code>MessageSourceResolvable</code>arg中包含的所有属性
     * (<code>locale</code>属性除外)</b>,
     * 尝试从<code>Context</code>中包含的<code>MessageSource</code>解析消息.<p>
     * 注意: 我们必须对此方法抛出<code>NoSuchMessageException</code>, 因为在调用此方法时,
     * 我们无法确定<code>defaultMessage</code>属性是否为null.
     *
     * @param resolvable value对象, 存储正确解析消息所需的4个属性.
     * @param locale     Locale用作"driver", 以确定要返回的消息.
     * @return message Resolved message.
     * @throws NoSuchMessageException 在任何locale中都找不到
     * @see <a href="http://java.sun.com/j2se/1.3/docs/api/java/text/MessageFormat.html">java.text.MessageFormat</a>
     */
    String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException;
}