package {{proxypackage}}

import functions.model.Serializer
import functions.model.Coordinates2
{{#allImports}}
import {{.}}
{{/allImports}}

class {{className}}{{frameworkTypeArgFull}}(
  {{#functions}}
  // {{functionN}} serialization
  {{functionN}}ToByteArray: {{methodParams}}.{{caseClassName}} => Array[Byte],
  {{functionN}}ReturnTypeFromByteArray: Array[Byte] => {{resultNNoFramework}},
  {{/functions}}
  // this should transport the data to the remote function and get the response from that function
  transport: (Coordinates2, Array[Byte]) => {{frameworkTypeArgOpen}}Array[Byte]{{frameworkTypeArgClose}}
) extends {{exportedTypeFull}}:

  {{#functions}}
  // {{functionN}} function
  private val coords{{methodParams}}{{caseClassName}} = {{methodParams}}.Methods.{{caseClassName}}
  def {{functionN}}({{params}}): {{resultN}} =
    val c  = {{caseClass}}({{paramsCall}})
    val binIn = {{functionN}}ToByteArray(c)
    {{#exportedType.hasFramework}}
    transport(coords{{methodParams}}{{caseClassName}}, binIn).map: binOut=>
      {{functionN}}ReturnTypeFromByteArray(binOut)
    {{/exportedType.hasFramework}}
    {{^exportedType.hasFramework}}
    val binOut = transport(coords{{methodParams}}{{caseClassName}}, binIn)
    {{functionN}}ReturnTypeFromByteArray(binOut)
    {{/exportedType.hasFramework}}
  {{/functions}}
