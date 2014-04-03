Compile time XML unmarshalling for Scala
=========

`clide.xml` is a set of scala macros to declaratively configure hierarchical data structures using xml files at compile time with static type checking.

The main motivation for the project is the separation of business logic and views. In conjunction with the `clide.reactive.ui` library (will be published shortly) it allows for declarative view bindings in the style of *angular.js* but without the runtime compilation overhead and with static type checks.

The main idea behind `clide.xml` is that an arbitrary Scala object serves as a schema definition. XML tags then represent method or constructor calls. Attributes represent either required parameters of the construction method or assignments to the constructed type.

Here comes a simple Schema example wich only supports html anchor tags:

    import clide.xml._
    import org.scalajs.dom
    
    object MySchema {
      def a() = dom.document.createElement("a").asInstanceOf[dom.HTMLAnchorElement]
    }
    
    XML.inline(MySchema,"<a>") // compile error: unclosed element `a`
    
    XML.inline(MySchema,"<b></b>) // compile error: unsupported element type `b`
    
    XML.inline(MySchema,"<a href="#"></a>") // expands to:
                                            // {
                                            //   lazy val elem$1 = MySchema.a()
                                            //   elem$1.href = "#"
                                            //   elem$1
                                            // }
                                               

As you can see, a Schema does not have to be anything special - It can be any Scala object!
Because `HTMLAnchorElement` has a settable Property `href` which takes a string, the above code does compile and run!

Instead of `XML.inline` there is also `XML.include` which takes a **relative file path instead of an XML string**. The file path is resolved relative to the source file wich references it. However, for the following examples, we will continue to use `XML.inline`.

Tag Constructors
---

Tag constructors are either methods or case class constructors:

    object Schema {
      def myTag() = ...                            // e.g. <myTag/>
      case class MyOtherTag(someAttribute: String) // e.g. <MyOtherTag someAttribute="Test/>
    }

Required Attributes
---

Required attributes are realized through method parameters:

    object Schema {
      def a(href: String) = ...
    }
    
    XML.inline(Schema,"<a/>") // compile error: element type `a` requires attribute `href: String`
    XML.inline(Schema,"<a href="#"></a>") // expands to:
                                          // {
                                          //   lazy val elem$1 = Schema.a("#")
                                          // }

**Note:** Default and named parameters are currently unsupported by scala macros. Therefore they are also unsupported in `clide.xml`. This will change with the release of scala 2.11.1

Nesting
---

For now, the support for nesting is only imperative. That means nested elements will be appended to their parents with `+=`. The expanded macro does still resolve implicits which means, that `+=` can be added through the pimp-my-library pattern to any type.

    object Schema {
      case class MyChild(name: String)
      case class MyParent(name: String) {
        var children = Buffer.empty[MyChild]
        def +=(child: MyChild) = children += child
        def +=(child: MyParent) = children += child
      }
    }
    
    XML.inline(Schema,"<MyParent name="Mother"><MyChild name="Son"/></MyParent>")
    // expands to:
    // {
    //   lazy val elem$1 = Schema.MyParent("Mother")
    //   lazy val elem$2 = Schema.MyChild("Son")
    //   lazy val elem
    //   elem$1 += elem$2
    //   elem$1
    // }

Plain Text Nodes
---

Plain text nodes are added as children of type string. (You could of course again introduce implicit conversions here)

    object Schema {
      case class Paragraph() {
        var text: String = ""
        def +=(text: String) = this.text += text
        def +=(paragraph: Paragraph) = this.text += paragraph.text
      }
    }
    
    XML.inline(Schema,"""
      <Paragraph>
        Some text
        <Paragraph>Nested</Paragraph>
      </Paragraph>
    """
    // expands to:
    // {
    //   lazy val elem$1 = Schema.Paragraph()
    //   elem$1 += "\n    Some text\n    "
    //   lazy val elem$2 = Schema.Paragraph()
    //   elem$2 += "Nested"
    //   elem$1 += elem$2
    //   elem$2
    // }
    
**Note:** Whitespace is not dropped in xml

Escaped Scala Code
---

You can include arbitrary scala code between braces:

    val subject = "dlroW"
    XML.inline(Schema,"<div width='{24 * 6}' height="{4}">Hello { subject.reverse }!</div>
    // expands to:
    // { 
    //    lazy val elem$1 = Schema.div()
    //    elem$1.width = 24 * 6
    //    elem$1 += "Hello "
    //    elem$1 += subject.reverse
    //    elem$1 += "!"
    //    elem$1
    // }
    
The code can be of any type. This way you can also set non-string attributes.

