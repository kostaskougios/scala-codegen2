package {{proxypackage}}
{{#allImports}}
import {{.}}
{{/allImports}}

class {{className}}{{frameworkTypeArgFull}}(
  {{#functions}}
    {{functionN}}Deserializer: Array[Byte] => {{methodParams}}.{{caseClassName}},
    {{functionN}}ReturnTypeSerializer: {{resultNNoFramework}} => Array[Byte],
  {{/functions}}
    f: {{exportedTypeFull}}
):

  /*
  override val invoke: PartialFunction[(String, Array[Byte]) , Array[Byte]] =
    {{#functions}}
      case ({{methodParams}}.Methods.{{caseClassName}}, data) => {{functionN}}(data)
    {{/functions}}
  */

  {{#functions}}
  {{#mapResults}}
  def {{functionN}}(data: Array[Byte]): {{frameworkTypeArg}}[Array[Byte]] =
    val params = {{functionN}}Deserializer(data)
    f.{{functionN}}({{#paramsRaw}}params.{{name}}{{^last}}, {{/last}}{{/paramsRaw}}).map: r=>
      {{functionN}}ReturnTypeSerializer(r)
  {{/mapResults}}
  {{^mapResults}}
  def {{functionN}}(data: Array[Byte]): Array[Byte] =
    val params = {{functionN}}Deserializer(data)
    val r      = f.{{functionN}}({{#paramsRaw}}params.{{name}}{{^last}}, {{/last}}{{/paramsRaw}})
    {{functionN}}ReturnTypeSerializer(r)
  {{/mapResults}}
  {{/functions}}
