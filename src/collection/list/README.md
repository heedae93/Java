# List 인터페이스와 다형성 (list 패키지)

`array` 패키지에서 만든 `MyArrayList<E>`와 `link` 패키지에서 만든 `MyLinkedList<E>`, 두 자료구조가 완성된 시점에서 자연스럽게 생기는 질문이 있다.

> **"두 자료구조를 바꿔 끼울 수 있게 하려면 어떻게 해야 할까?"**

이 패키지는 그 질문에 답하는 과정이다.
`MyList<E>` 인터페이스를 정의해 두 구현체를 하나로 묶고, **인터페이스를 통한 다형성**이 어떤 실질적인 이점을 주는지 — 특히 자료구조 선택의 유연성 측면에서 — 학습한다.

---

## 1단계. 공통 계약 정의 — `MyList<E>`

`MyArrayList` 와 `MyLinkedList` 는 기능은 같지만 내부 구조가 다르다.
둘을 교체 가능하게 만들려면 **동일한 메서드 목록을 계약(interface)으로 선언**해야 한다.

```java
public interface MyList<E> {
    int size();
    void add(E e);
    void add(int index, E e);
    E get(int index);
    E set(int index, E element);
    E remove(int index);
    int indexOf(E o);
}
```

- `array` / `link` 패키지에서 각각 따로 만들었던 메서드들을 하나의 인터페이스로 통합했다.
- 이제 이 인터페이스만 보면 어떤 List 구현체든 어떤 메서드를 제공하는지 알 수 있다.

---

## 2단계. 인터페이스 구현 — `MyArrayList<E>`, `MyLinkedList<E>`

두 클래스 모두 `implements MyList<E>` 를 선언해 인터페이스를 구현한다.

### `MyArrayList<E>`
`array` 패키지의 V4(제네릭)에서 완성된 구현체다. 핵심 특성:
- 내부에 `Object[]` 배열을 두고, 가득 차면 `grow()` 로 2배 확장.
- `get(index)` 는 O(1) — 인덱스로 메모리 주소를 바로 계산한다.
- 중간 삽입/삭제는 O(n) — 데이터를 밀거나 당겨야 한다.

### `MyLinkedList<E>`
`link` 패키지의 V3(제네릭)에서 완성된 구현체다. 핵심 특성:
- 내부에 `Node<E>` 체인을 두고, 노드를 새로 만들어 연결하므로 크기 제한이 없다.
- `get(index)` 는 O(n) — 첫 노드부터 하나씩 따라가야 한다.
- 맨 앞 삽입/삭제는 O(1) — 참조 교체만으로 완료된다.

> **포인트**: 두 클래스 모두 `MyList<E>` 를 구현하므로, `MyList<E>` 타입 변수 하나로 어느 쪽이든 받을 수 있다.

---

## 3단계. 다형성 활용 — `BatchProcessor`

인터페이스가 실제로 어떤 이점을 주는지 보여주는 핵심 예제다.

```java
public class BatchProcessor {
    private final MyList<Integer> list;  // 구현체가 아닌 인터페이스 타입으로 받는다

    public BatchProcessor(MyList<Integer> list) {
        this.list = list;
    }

    public void logic(int size) {
        for (int i = 0; i < size; i++) {
            list.add(0, i);  // 항상 맨 앞에 추가
        }
    }
}
```

`BatchProcessor` 는 `MyList<E>` 인터페이스에만 의존한다. 내부에서 `ArrayList` 인지 `LinkedList` 인지 전혀 알지 못한다.

### `BatchProcessorMain` — 올바른 자료구조 선택

```java
// MyArrayList<Integer> list = new MyArrayList<>();  // 앞에 추가 → O(n), 느림
MyLinkedList<Integer> list = new MyLinkedList<>();   // 앞에 추가 → O(1), 빠름

BatchProcessor processor = new BatchProcessor(list);
processor.logic(50_000);
```

- `BatchProcessor.logic()` 은 **항상 맨 앞에 추가**하는 작업이다.
- `MyArrayList` 는 앞에 추가할 때마다 모든 데이터를 한 칸씩 밀어야 해서 O(n) — 50,000번이면 매우 느리다.
- `MyLinkedList` 는 맨 앞 삽입이 참조 교체만으로 O(1) — 훨씬 빠르다.
- 이처럼 **인터페이스 덕분에 `BatchProcessor` 코드를 한 줄도 바꾸지 않고** 구현체만 바꿔 성능을 개선할 수 있다.

> **인터페이스의 실질적 가치**: 코드의 변경 없이 동작(구현)을 교체할 수 있다. 올바른 자료구조를 선택하는 것이 얼마나 중요한지도 함께 보여준다.

---

## 4단계. 성능 비교 테스트

이론으로 배운 시간 복잡도가 실제로도 차이가 나는지 측정한다.

### `MyListPerformanceTest` — 직접 만든 구현체 비교

`MyArrayList` vs `MyLinkedList` 를 50,000건 데이터로 추가·조회·검색 시간을 측정한다.

| 연산 | MyArrayList | MyLinkedList | 이유 |
|------|:-----------:|:------------:|------|
| 앞에 추가 (`add(0, e)`) | 느림 | **빠름** | 배열은 O(n) 이동, 노드는 O(1) 참조 교체 |
| 중간 추가 (`add(i/2, e)`) | 느림 | 느림 | 둘 다 평균 O(n) |
| 뒤에 추가 (`add(e)`) | **빠름** | 느림 | 배열은 O(1), 노드는 마지막 노드 탐색 O(n) |
| index 조회 (`get(index)`) | **빠름** | 느림 | 배열 O(1) vs 노드 순회 O(n) |
| 값 검색 (`indexOf`) | 비슷 | 비슷 | 둘 다 O(n) 순회 |

### `JavaListPerformanceTest` — 자바 표준 라이브러리 비교

동일한 항목을 `java.util.ArrayList` vs `java.util.LinkedList` 로 측정한다.
결과 패턴은 위와 동일하지만, 자바 내장 구현은 최적화가 되어 있어 절대적인 수치가 더 빠르다.

> **실무 인사이트**: 이론상 `LinkedList` 가 앞 삽입에서 유리하지만,
> 현대 CPU의 **캐시 효율(메모리 연속성)** 때문에 실제로는 `ArrayList` 가 더 빠른 경우가 많다.
> 자바 표준 라이브러리 결과에서 이 차이를 직접 확인할 수 있다.

---

## 실습 예제

### test/ex1 — 배열에서 List로 마이그레이션

`ArrayEx1` → `ListEx1` → `ListEx2` → `ListEx3` 순서로, **고정 배열의 불편함을 List가 어떻게 해소하는지** 체험한다.

| 파일 | 방식 | 핵심 포인트 |
|------|------|------------|
| `ArrayEx1` | `int[]` 배열 | 크기 고정, 직접 인덱스 관리 |
| `ListEx1` | `ArrayList<Integer>` | `add()` / `get()` / `size()` 로 대체, for 인덱스 순회 |
| `ListEx2` | `ArrayList<Integer>` + Scanner | 개수를 모를 때 — 0 입력 전까지 동적으로 추가 |
| `ListEx3` | `ArrayList<Integer>` + 향상된 for | `for (int n : numbers)` — 향상된 for문으로 순회 |

### test/ex2_old — 배열 주문 관리 → ArrayList 주문 관리

`ProductOrder` 객체를 담는 자료구조를 배열에서 ArrayList로 교체하는 과정을 보여준다.

```
ProductOrderTestV1 (배열)          ProductOrderTestV2 (ArrayList)
─────────────────────────────      ─────────────────────────────
"몇 개 입력하시겠습니까?" → n      상품명 입력 → 'x' 입력 시 종료
ProductOrder[] orders = new [n]    ArrayList<ProductOrder> orders
→ 개수를 미리 알아야 한다          → 개수를 몰라도 된다
printOrders(ProductOrder[])        printOrders(List<ProductOrder>)  ← List 타입으로 받음
```

- V2에서 `printOrders` / `getTotalAmount` 의 매개변수 타입이 `ArrayList` 가 아닌 `List<ProductOrder>` 임에 주목.
- **구현체가 아닌 인터페이스 타입으로 받는 것**이 좋은 습관이다.

### test/ex2 — OOP 설계로 리팩터링 (ShoppingCart)

ex2_old의 절차적 코드를 객체 지향 방식으로 재설계한 버전이다.

```java
// Item — 상품 데이터 + 책임을 캡슐화
class Item {
    private String name;
    private int price;
    private int quantity;
    public int getTotalPrice() { return price * quantity; }  // 계산 책임을 Item이 가짐
}

// ShoppingCart — List<Item> 을 관리하는 책임
class ShoppingCart {
    private List<Item> items = new ArrayList<>();
    public void addItem(Item item) { ... }
    public void displayItems() { ... }         // 출력 + 합계 계산
    private int calculateTotalPrice() { ... }  // 전체 합산
}
```

- `Item` 이 `getTotalPrice()` 를 가지므로 외부에서 `price * quantity` 를 계산할 필요가 없다.
- `ShoppingCart` 가 `List` 를 내부에 감추고 add / display 책임을 담당한다.
- 이전 버전(`ex2_old`)과 비교하면 **책임 분리와 캡슐화**의 차이가 명확하게 보인다.

---

## 전체 흐름 요약

| 단계 | 무엇을 배웠는가 |
|:----:|----------------|
| **MyList 인터페이스** | 두 구현체를 하나의 계약으로 묶는다 |
| **MyArrayList / MyLinkedList 구현** | `implements MyList<E>` 로 교체 가능한 구현체가 된다 |
| **BatchProcessor** | 인터페이스 덕분에 코드 변경 없이 구현체를 교체 — 다형성의 실질적 가치 |
| **성능 비교** | 이론(시간 복잡도)을 실측으로 검증 — 올바른 자료구조 선택의 중요성 |
| **실습 예제** | 배열 → List 마이그레이션, 절차적 → OOP 설계 체험 |

### 핵심 교훈

- **인터페이스로 의존하라**: `MyList<E>` 타입으로 받으면, 구현체를 바꿔도 사용하는 코드는 전혀 수정하지 않아도 된다.
- **자료구조 선택 기준**:
  - 조회가 빈번하다면 → `ArrayList` (`get` O(1))
  - 맨 앞 삽입/삭제가 빈번하다면 → `LinkedList` (`addFirst` O(1))
  - 실무에서는 대부분 `ArrayList` — CPU 캐시 효율이 이론적 차이를 상쇄한다.
- **OOP 설계**: 데이터와 그 데이터를 다루는 행동은 같은 클래스에 두자 (`Item.getTotalPrice()`).
