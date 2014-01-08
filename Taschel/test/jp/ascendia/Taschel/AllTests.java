package jp.ascendia.Taschel;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * 以下のような記述で、複数のテストを一度に実行することが可能です。
 * 実行したいてテストクラスをカンマ区切りで、@SuiteClassesアノテーションで記述します。
 * 
 * このようなクラスをテストスイートと呼びます。
 * ※スイートはスイートルームのSuiteと同じで「一続きの」の意。甘いSweetではありません
 * 
 * @author 斉藤 祐輔
 *
 */
@RunWith(Suite.class)
@SuiteClasses({ 
		LoginServletMock.class, 
		LoginServletMockWithServletTester.class
	})
public class AllTests {

}
