# 第二条：遇到多个构造器参数时要考虑构建器
对于有多个参数和可选变量的类，应该用哪种构造器或者静态工厂来编写呢？  
程序员一向习惯采用重叠构造器(telescoping constructor)模式，在这种模式下，提供的第一个构造器只有
必要的参数，第二个构造器有一个可选参数，第三个有两个可选参数，以此类推。  
例如：
```java
public class NutritionFacts {
    private final int servingSize; // required
    private final int servings; // required
    private final int calories; // optional
    private final int sodium; // optional
    private final int fat; // optional
    private final int carbohydrate; // optional

    public NutritionFacts(int servingSize, int servings) {
        this(servingSize, servings, 0);
    }
    
    public NutritionFacts(int servingSize, int servings, int calories) {
        this(servingSize, servings, calories, 0);
    }

    public NutritionFacts(int servingSize, int servings, int calories, int sodium) {
        this(servingSize, servings, calories, sodium, 0);
    }

    public NutritionFacts(int servingSize, int servings, int calories, int sodium, int fat) {
        this(servingSize, servings, calories, sodium, fat, 0);
    }


    public NutritionFacts(int servingSize, int servings, int calories, int sodium, int fat, int carbohydrate) {
        this.servingSize = servingSize;
        this.servings = servings;
        this.calories = calories;
        this.sodium = sodium;
        this.fat = fat;
        this.carbohydrate = carbohydrate;
    }
}
```
当你想要创建实例的时候，就利用参数列表最短的构造器，但该列表中包含了要设置的所有参数：
```java
    NutritionFacts cocaCola = 
                    new NutritionFacts(240, 8, 100, 0, 25, 27);
```
这个构造器调用通常需要许多你根本不想设置的参数，但不得不为他们传递值。在这个例子中，
我们给sodium传递了一个值为0。如果”仅仅“是这6个参数，看起来不算太糟糕，问题是随着
参数数目的增加，他很快就会失去控制。  

**简而言之，重叠构造器模式可行，但是当有许多参数的时候，客户端代码会很难编写，并且依然难以阅读。**  
如果读者想知道那些值是什么意思，必须很仔细地数着这些参数来探个就叫。如果不小心颠倒了其中两个
参数的顺序，编译器也不会出错，但是程序在运行时会出现错误的行为(详见[第51条]())。  

还有第二种代替办法，即JavaBeans模式，在这种模式下，先调用一个无参构造器来创建对象，然后再
调用setter方法来设置每个必要的参数，以及每个相关的可选参数：  
```java
public class NutritionFactsJavaBeans {
    private int servingSize = -1; // Required no default value
    private int servings = -1; // Required no default value
    private int calories = 0;
    private int fat = 0;
    private int sodium = 0; 
    private int carbohydrate = 0;

    public NutritionFactsJavaBeans() {
    }

    public void setServingSize(int servingSize) {
        this.servingSize = servingSize;
    }

    public void setServings(int servings) {
        this.servings = servings;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public void setFat(int fat) {
        this.fat = fat;
    }

    public void setSodium(int sodium) {
        this.sodium = sodium;
    }

    public void setCarbohydrate(int carbohydrate) {
        this.carbohydrate = carbohydrate;
    }
}
```
这种模式弥补了重叠构造器模式的不足。说的明白一点，就是创建实例很容易，这样产生的代码阅读起来也很容易  
```java
        NutritionFactsJavaBeans cocaCola = new NutritionFactsJavaBeans();
        cocaCola.setServingSize(240);
        cocaCola.setServings(8);
        cocaCola.setCalories(100);
        cocaCola.setSodium(35);
        cocaCola.setCarbohydrate(27);
```
遗憾的是，JavaBeans模式自身有着很严重的缺点。因为构造过程被分到了几个调用中，
**在构造过程中JavaBean可能处于不一致的状态。** 类无法仅仅通过校验构造器参数的有效性来保证一致性。
试图使用处于不一致状态的对象将会导致失败，这种失败与包含错误的代码大相径庭，因此调试起来十分困难。
与此相关的另一点不足在于，**JavaBeans模式使得把类做成不可变的可能性不存在** (详见[第17条]()),
这就需要程序员付出额外的努力来确保它的线程安全。  

当对象的构造完成前手工冻结，并且不允许在冻结前使用，可以手工弥补这些不足。但这种方式十分笨拙，
在实践中很少使用。此外，它甚至会在运行时导致错误，因为编译器无法确保程序员会在使用前先调用
对象上的freeze方法进行冻结。  

幸运的是，还有第三种替代方法，他既能保证像重叠构造器模式那样的安全性，也能保证JavaBeans模式的可读性。
这就是[建造者(Builder)模式]()的一种形式。他不生成想要的对象，而是让客户端利用所有必要的参数
调用构造器(或者静态工厂)，得到一个builder对象。然后客户端在builder对象上调用类似于setter的方法，
来设置每个相关的可选参数。最后客户端通过调用无参的build方法来生成通常是不可变的对象。
这个builder通常是它构建的类的静态成员类(详见[第24条]())。下面就是它的示例：
```java
public class NutritionFactsBuilderPatten {
    private final int servingSize;
    private final int servings;
    private final int calories;
    private final int fat;
    private final int sodium;
    private final int carbohydrate;
    
    public static class Builder {
        // required parameters
        private final int servingSize;
        private final int servings;
        
        // Optional parameters - initialized to default values
        private int calories = 0;
        private int fat = 0;
        private int sodium = 0;
        private int carbohydrate = 0;
        
        public Builder(int servingSize, int servings) {
            this.servingSize = servingSize;
            this.servings = servings;
        }
        
        public Builder calories(int val) {
            calories = val;
            return this;
        }
        
        public Builder fat(int val) {
            fat = val;
            return this;
        }
        
        public Builder sodium(int val) {
            sodium = val;
            return this;
        }
        
        public Builder carbohydrate(int val) {
            carbohydrate = val;
            return this;
        }
        
        public NutritionFactsBuilderPatten build() {
            return new NutritionFactsBuilderPatten(this);
        }
    }
    
    private NutritionFactsBuilderPatten(Builder builder) {
        servingSize = builder.servingSize;
        servings = builder.servings;
        calories = builder.calories;
        fat = builder.fat;
        sodium = builder.sodium;
        carbohydrate = builder.carbohydrate;
    }
}
```
注意NutritionFacts是不可变的，所有的默认参数都单独放在一个地方。builder的设值方法返回builder本身，
以便把调用链接起来，得到一个流式API。下面就是其客户端代码：
```java
        NutritionFactsBuilderPatten cocaCola = new Builder(240, 8)
                .calories(100).sodium(35).carbohydrate(27).build();
```
这样的客户端代码很容易编写，更为重要的是易于阅读。**Builder模式模拟了具名的可算参数，**
就像Python和Scala编程语言一样。  
为了简洁起见，示例中省略了有效性检查。要想尽快检查到无效参数，可以在builder的构造器和方法中
检查参数的有效性。查看不可变量，包括build方法调用的构造器中的多个参数。为了确保这些不变量免受攻击，
从builder复制完参数后，要检查对象域(详见[第50条]())。如果检查失败，就抛出
IllegalArgumentException(详见[第72条]())，其中的详细信息会说明哪些参数是无效的(详见[第75条]())。  

**Builder模式也适用于类层次结构。** 使用平行层次结构的builder时，各自嵌套在相应的类中。
抽象类有抽象的builder，具体类有具体的builder。假设用类层次根部的一个抽象类来表示各式各样的披萨：
```java
public abstract class Pizza {
    /**
     * pizza 调料
     */
    public enum Topping {HAM, MUSHROOM, ONION, PEPPER, SAUSAGE}

    final Set<Topping> toppings;

    abstract static class Builder<T extends Builder<T>> {
        EnumSet<Topping> toppings = EnumSet.noneOf(Topping.class);
        public T addTopping(Topping topping) {
            toppings.add(Objects.requireNonNull(topping));
            return self();
        }

        abstract Pizza build();
        // Subclasses must override this method to return ” this"
        protected abstract T self();
    }

    Pizza(Builder<?> builder) {
        toppings = builder.toppings.clone();
    }
}
```
注意,Pizza.Builder的类型是泛型(generic type)，带有一个递归类型参数(recursive type parameter)，
详见[第30条]()。他和抽象的self方法一样，允许在子类中适当地进行方法链接，不需要转换类型。
这个针对Java缺乏self类型的解决方案，被称为模拟的self类型(simulated self-type)  

这里有两个具体的Pizza子类，其中一个表示经典纽约风味的披萨，另一个表示馅料内置的半月形(calzone)披萨。
前者需要一个尺寸参数，后者则需要你指定酱汁应该是内置还是外置：
```java
/**
 * 经典纽约风味Pizza
 */
public class NyPizza extends Pizza {
    public enum Size {SMALL, MEDIUM, LARGE}
    private final Size size;

    public static class Builder extends Pizza.Builder<Builder> {
        private final Size size;

        public Builder(Size size) {
            this.size = Objects.requireNonNull(size);
        }

        @Override
        public NyPizza build() {
            return null;
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    private NyPizza(Builder builder) {
        super(builder);
        size = builder.size;
    }
}

/**
 * 半月形 披萨
 */
public class Calzone extends Pizza {
    private final boolean sauceInside;

    public static class Builder extends Pizza.Builder<Builder> {
        private boolean sauceInside = false; // Default

        public Builder sauceInside() {
            sauceInside = true;
            return this;
        }

        @Override
        public Calzone build() {
            return new Calzone(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    private Calzone(Builder builder) {
        super(builder);
        sauceInside = builder.sauceInside;
    }
}
```
注意，每个子类的构建器的build方法，都声明返回正确的子类：NyPizza.Builder的build方法返回NyPizza，
而Calzone.Builder中的则返回Calzone。在该方法中，子类方法声明返回超级类中声明的返回类型的子类型，
这被称作协变返回类型(covariant return type)。它允许客户端无需转换类型就能使用这些构造器。  

这些”层次化构建器“的客户端代码本质上与简单的NutritionFacts构建器一样。为了简洁，
下列客户端代码假设是在枚举常量上静态导入：
```java
        NyPizza pizza = new NyPizza.Builder(SMALL)
                .addTopping(SAUSAGE).addTopping(ONION).build();
        
        Calzone calzone = new Calzone.Builder()
                .addTopping(HAM).sauceInside().build();
```

与构造器相比，builder的微略优势在于，它可以有多个可变(varargs)参数。因为builder是利用
单独的方法来设置每一个参数。此外，构造器还可以将多次调用每一个方法而传入的参数集中到一个域中，
如前面调用了两次addTopping方法的代码所示。  

Builder模式十分灵活，可以利用单个builder构建多个对象。builder的参数可以在调用build方法
创建对象之前的期间进行调整，也可以随着不同的对象而改变。builder可以自动填充某些域，
例如每次创建对象时自动添加序列号。  

Builder模式的确也有它自身的不足。为了创建对象，必须先创建他的构建器。虽然创建这个构建器的开销
在实践中可能没有那么明显，但是在某些十分注重性能的情况下，可能就成问题了。Builder模式还比
重叠构造器模式更加冗长，因此他只在有很多参数的时候才使用，比如4个或者更多个参数。
但是记住，将来你可能需要添加参数。如果一开始就是用构造器或者静态工厂，等到类需要多个参数时
才添加构建器，就会无法控制，这些过时的构建器或者静态工厂就会显得十分不协调。
因此，通常最好一开始就使用构建器。  

**如果类的构造器或者静态工厂中具有多个参数，设计这种类时，Builder模式就是一种不错的选择，**
特别时当大多数参数都是可选或者类型相同的时候。与使用重叠构造器模式相比，使用Builder模式的
客户端代码将更易于阅读和编写，构建器也比JavaBeans更安全。


