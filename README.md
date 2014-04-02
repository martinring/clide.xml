A compile time XML library for Scala
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

Nesting
---

For now, the support for nesting is only imperative. That means nested elements will be appended to their parents with `+=`. The expanded macro does still resolve implicits which means, that `+=` can be added through the pimp-my-library pattern to any type.

    object Schema {
      case class MyChild(name: String)
      case class MyParent(name: String) {
        var children = Buffer.empty[MyChild]
        def +=(child: MyChild) = children += child
      }
    }
    
    XML.inline(Schema,"<MyParent name="Mother"><MyChild name="Son"/></MyParent>")
    // expands to:
    // {
    //   lazy val elem$1 = Schema.MyParent("Mother")
    //   lazy val elem$2 = Schema.MyChild("Son")
    //   elem$1 += elem$2
    //   elem$1
    // }
