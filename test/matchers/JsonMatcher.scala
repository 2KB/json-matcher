package matchers

import org.specs2.matcher._
import play.api.libs.json._

/**
  * Specs2用のHelperクラスです。Specクラスにて継承して使用します
  *
  * Json用のカスタムMatcherなどを提供します
  * Matcher変換用のimplicitを用意するために、MatchersImplicitsを継承しています
  */
trait JsonMatcher extends MatchersImplicits {

  /**
    * Jsonを比較するMatcherを生成します
    *
    * 使用例：
    * actual must equalToJsVal(expected)
    *
    * @param expected               期待値
    * @param useFullValueForMessage 比較対象値の全てをKOメッセージに含めるか否か
    * @return JsValue比較用Matcher
    */
  def equalToJson(expected: JsValue, useFullValueForMessage: Boolean = true): Matcher[JsValue] = {
    // @param actual 実際の値。渡し方はメソッドコメントの使用例参照。
    (actual: JsValue) => {
      val koMessage = createKoMessageForEqualToJsonDetail(actual, expected, useFullValueForMessage)
      val okMessage = "ok"

      // koMessageの値が存在する時は、マッチしなかった値があるということになります
      (koMessage.isEmpty, okMessage, koMessage.getOrElse("dummy"))
    }
  }

  /**
    * 指定されたJsonを比較し、値が異なった要素に対して比較エラーメッセージを返却する
    *
    * 全て合っていた場合は、Noneを返却する
    *
    * @param actual                 実際の値
    * @param expected               期待値
    * @param useFullValueForMessage 比較対象値の全てをKOメッセージに含めるか否か
    * @return 比較エラーメッセージ
    */
  def createKoMessageForEqualToJsonDetail(actual: JsValue, expected: JsValue, useFullValueForMessage: Boolean = true): Option[String] = {
    // 最初にアサートエラーとなる項目の失敗メッセージを取得する
    val koMessage = createFirstKoMessageForEqualToJson(expected, actual)
    koMessage match {
      case Some(x) => {
        if (useFullValueForMessage) {

          // Jsonの値を比べやすくするために、オブジェクトのフィールドをソートして揃える
          val expectedSorted = sortJsObj(expected)
          val actualSorted = sortJsObj(actual)

          // 比較対象値の全てを使って、詳細なKOメッセージを作成する
          val detailMessage =
          s"""
             |-----
             |Actual detail   :
             |${Json.prettyPrint(actualSorted)}
             |
             |Expected detail :
             |${Json.prettyPrint(expectedSorted)}
             |-----
             |
           |at""".stripMargin
          Some(x + detailMessage)
        } else {
          Some(x + "\n\nat")
        }
      }
      case None => None
    }
  }

  /**
    * 指定されたJsonの要素を比較していき、最初に値が異なったものに対して比較エラーメッセージを返却する
    * 全て合っていた場合は、Noneを返却する
    *
    * @param expected この項目の期待値
    * @param actual   この項目の実際の値
    * @param paths    この項目の要素までのパス
    * @return 比較エラーメッセージ
    */
  def createFirstKoMessageForEqualToJson(expected: JsValue, actual: JsValue, paths: Seq[String] = Seq()): Option[String] = {

    (expected, actual) match {
      case (JsObject(eFields), JsObject(aFields)) => {
        // フィールド名でグループ化
        val eKeys = eFields.map { case (path, value) => path }
        val aKeys = aFields.map { case (path, value) => path }

        val fullKeys = (eKeys ++ aKeys).toList.distinct.sorted

        val eFieldMap = eFields.toMap
        val aFieldMap = aFields.toMap

        val results = fullKeys.map { path => createFirstKoMessageForEqualToJson(eFieldMap.getOrElse(path, JsNull), aFieldMap.getOrElse(path, JsNull), paths :+ path) }
        // NGだったものの最初の一件。もしくはNone
        results.filter(_.isDefined).headOption.getOrElse(None)
      }

      case (JsArray(eItems), JsArray(aItems)) => {

        // 配列要素をそれぞれ比較。配列の一方が他方より短い場合は、一方の要素をJsNullとして比較する。
        val results = eItems.zipAll(aItems, JsNull, JsNull).zipWithIndex.map { case ((eItem, aItem), i) =>
          // pathには、配列要素の何番目なのかを指定する
          val path = s"array($i)"
          createFirstKoMessageForEqualToJson(eItem, aItem, paths :+ path)
        }

        // NGだったものの最初の一件。もしくはNone
        return results.filter(_.isDefined).headOption.getOrElse(None)
      }
      // どちらかがobjectでもarrayでもなければ、比較を行う
      case _ => {
        if (actual == expected) {
          None
        } else {
          val message =
            s"""Each json not match.
                |
                |Path     : ${paths.mkString("->")}
                |Actual   : ${actual}
                |Expected : ${expected}""".stripMargin
          return Some(message)
        }
      }
    }
  }

  /**
    * JsObjectをソートする。配列要素の順番はそのまま
    *
    * @param js ソート対象値
    * @return ソート後の値
    */
  def sortJsObj(js: JsValue): JsValue = js match {
    // object型の場合、フィールドのキーでソート
    case JsObject(fields) => JsObject(
      fields.toList.sortBy(_._1).map { case (k, v) =>
        // フィールドのバリューは、またオブジェクト型の可能性があるので、もう一度ソート
        (k, sortJsObj(v))
      }
    )
    // 配列の場合、要素自体はソートしないが、中身がオブジェクト型の可能性があるので、中身を一つずつソート
    case JsArray(arr) => JsArray(arr.map(sortJsObj(_)))
    case _ => js
  }
}
