# 직접 만들어보는 LinkedList (link 패키지)

노드(Node)의 개념에서 출발해서, 자바 `LinkedList`와 유사한 자료구조를 단계별(V1 → V3)로 직접 구현해 나가는 과정을 정리한 문서다.
각 단계는 `MyLinkedListVx` (구현) 와 `MyLinkedListVxMain` (사용 예시) 으로 구성되어 있고,
**"이 단계에서 무엇을 했고, 어떤 문제가 남아서 다음 버전으로 넘어갔는가"** 를 중심으로 설명한다.

---

## 0단계. 노드의 기초 — `Node`, `NodeMain1`, `NodeMain2`, `NodeMain3`

LinkedList를 만들기 전에, 그 핵심 구성 요소인 **노드(Node)** 의 구조와 연결 방식을 먼저 이해한다.

### `Node` — 데이터와 다음 노드를 담는 단위

```java
class Node {
    Object item;  // 실제 데이터
    Node next;    // 다음 노드를 가리키는 참조
}
```

- 데이터(`item`)와 다음 노드를 가리키는 참조(`next`) 두 가지만 갖는다.
- `next` 가 `null` 이면 마지막 노드다.
- `toString()` 은 `next` 를 따라가며 `[A->B->C]` 형태로 체인 전체를 출력한다.

### `NodeMain1` — 노드를 직접 연결하고 순회하기

```java
Node first = new Node("A");
first.next = new Node("B");
first.next.next = new Node("C");
// 결과: A -> B -> C
```

- `next` 참조를 손으로 엮어서 A → B → C 체인을 만든다.
- 순회는 `x = x.next` 를 반복해 `null` 이 될 때까지 이어가면 된다.

### `NodeMain2` — `toString()` 으로 체인 전체 출력

- `Node.toString()` 이 `next` 를 재귀적으로 따라가므로, `first` 하나만 출력해도 연결된 모든 노드가 보인다.

### `NodeMain3` — 노드 조작 함수들의 구현

| 기능 | 구현 방법 | 시간 복잡도 |
|------|-----------|:-----------:|
| 전체 순회 (`printAll`) | `x = x.next` 반복 | O(n) |
| 마지막 노드 조회 (`getLastNode`) | `x.next == null` 까지 이동 | O(n) |
| 특정 index 노드 조회 (`getNode`) | index 횟수만큼 `x.next` 이동 | O(n) |
| 마지막에 추가 (`add`) | 마지막 노드의 `next` 에 새 노드 연결 | O(n) |

> **핵심 관찰**: 노드 체인은 **배열처럼 크기가 고정되지 않는다**. 새 노드를 만들어 `next` 로 연결하기만 하면 얼마든지 늘어난다.
> 하지만 모든 조작에서 체인을 앞에서부터 한 칸씩 따라가야 하므로, 탐색 비용이 O(n)임을 기억해야 한다.
> → 이 노드 조작 코드를 클래스로 감싸서 편리하게 쓸 수 있게 만들어보자.

---

## V1. 노드를 감싼 기본 링크드리스트 — `MyLinkedListV1`

노드 체인을 `first` 필드로 감추고, `add` / `get` / `set` / `indexOf` / `size` 기능을 메서드로 제공한다.

```java
private Node first;   // 첫 번째 노드 (head)
private int size = 0; // 현재 데이터 개수
```

- `add(e)` 는 `getLastNode()` 로 마지막 노드를 찾아 `next` 에 새 노드를 연결한다.
- `get(index)` / `set(index, e)` 는 `getNode(index)` 로 index 위치까지 체인을 따라가 접근한다.
- **크기 제한이 없다** — 배열과 달리 `grow()` 같은 확장 로직이 필요 없다. 노드를 새로 만들어 연결하면 된다.

### 성능 특성

| 연산 | 복잡도 | 이유 |
|------|:------:|------|
| `add(e)` (맨 뒤에 추가) | O(n) | 마지막 노드까지 순회해야 함 |
| `get(index)` / `set(index, e)` | O(n) | index 위치까지 한 칸씩 이동 |
| `indexOf(o)` | O(n) | 처음부터 끝까지 순회 |

> **ArrayList와의 차이**: ArrayList는 `get(index)` 가 O(1)인 반면, LinkedList는 항상 O(n)이다.
> 배열은 인덱스만 알면 메모리 주소를 바로 계산할 수 있지만, 노드 체인은 첫 노드부터 하나씩 따라가야 하기 때문이다.

### ⚠️ V1의 문제점

- 추가는 오직 **맨 뒤에만** 가능하다.
- **원하는 위치에 삽입**하거나 **삭제**하는 기능이 없다.

> **→ 해결 과제**: 중간 삽입(`add(index, e)`)과 삭제(`remove(index)`)를 추가하자. (V2)

---

## V2. 중간 삽입 · 삭제 기능 추가 — `MyLinkedListV2`

특정 index에 삽입/삭제하는 기능을 추가했다. LinkedList의 핵심 장점인 **참조(next) 교체만으로 삽입/삭제** 가 가능하다.

### 추가된 기능 — `add(int index, Object e)`

```java
public void add(int index, Object e) {
    Node newNode = new Node(e);
    if (index == 0) {
        // 맨 앞에 삽입: 새 노드가 기존 first를 가리키고, first를 새 노드로 교체
        newNode.next = first;
        first = newNode;
    } else {
        // 중간/끝에 삽입: 삽입 위치 이전 노드(prev)를 찾아 next 교체
        Node prev = getNode(index - 1);
        newNode.next = prev.next;
        prev.next = newNode;
    }
    size++;
}
```

### 추가된 기능 — `remove(int index)`

```java
public Object remove(int index) {
    Node removeNode = getNode(index);
    Object removedItem = removeNode.item;
    if (index == 0) {
        first = removeNode.next;   // head를 다음 노드로 교체
    } else {
        Node prev = getNode(index - 1);
        prev.next = removeNode.next;  // prev가 삭제 노드를 건너뛰도록 연결
    }
    removeNode.item = null;  // GC를 위해 참조 제거
    removeNode.next = null;
    size--;
    return removedItem;
}
```

### 성능 특성 (`MyLinkedListV2Main`)

| 연산 | 위치 | 복잡도 | 이유 |
|------|------|:------:|------|
| `add(index, e)` | 맨 앞 (index=0) | **O(1)** | 참조 교체만 하면 됨, 이동 불필요 |
| `add(index, e)` | 중간/끝 | O(n) | prev 노드까지 순회 필요 |
| `remove(index)` | 맨 앞 (index=0) | **O(1)** | first 교체만 하면 됨 |
| `remove(index)` | 중간/끝 | O(n) | prev 노드까지 순회 필요 |

> **ArrayList와 비교**: ArrayList는 중간 삽입/삭제 시 데이터를 **밀거나 당겨야** 해서 O(n)의 데이터 이동 비용이 발생한다.
> LinkedList는 **참조(next) 하나만 교체**하면 되므로, 삽입/삭제 자체 비용은 O(1)이다. (단, 위치를 찾는 순회 비용 O(n)은 여전히 있다.)
> 특히 맨 앞 삽입/삭제는 순회도 필요 없어 완전히 O(1)이다 — 이것이 LinkedList의 핵심 강점이다.

### ⚠️ V2의 문제점 — 타입 안전성

- 내부가 `Object` 기반이라 아무 타입이나 섞여 들어갈 수 있다.
- `get()` 이 `Object` 를 반환하므로 꺼낼 때마다 다운캐스팅이 필요하고, 잘못된 타입이 섞이면 런타임에 `ClassCastException` 이 발생한다.

> **→ 해결 과제**: 제네릭(Generic)을 도입해 컴파일 시점에 타입을 보장하자. (V3)

---

## V3. 제네릭 도입 — `MyLinkedListV3<E>`

클래스에 **타입 매개변수 `<E>`** 를 도입했다. `Node` 도 `Node<E>` 로 바꿔 내부까지 타입이 일관되게 흐르도록 했다.

```java
public class MyLinkedListV3<E> {

    private static class Node<E> {   // 내부 정적 클래스로 Node도 제네릭화
        E item;
        Node<E> next;
    }

    public void add(E e) { ... }         // E 타입만 받음
    public E get(int index) { ... }      // E 타입으로 반환 (다운캐스팅 불필요)
    public E set(int index, E element) { ... }
    public E remove(int index) { ... }
    public int indexOf(E o) { ... }
}
```

### V2의 문제가 어떻게 해결되는가 (`MyLinkedListV3Main`)

```java
MyLinkedListV3<String> stringList = new MyLinkedListV3<>();
stringList.add("a");
String s = stringList.get(0);   // 캐스팅 없이 String으로 바로 받음

MyLinkedListV3<Integer> intList = new MyLinkedListV3<>();
intList.add(1);
Integer i = intList.get(0);     // 캐스팅 없이 Integer로 바로 받음
```

- **타입 안전성 (컴파일 시점)**: `MyLinkedListV3<String>` 에 `add(123)` 을 시도하면 **컴파일 에러** → 잘못된 타입이 애초에 들어갈 수 없다.
- **다운캐스팅 불필요**: `get()` 이 `E` 를 반환하므로 꺼낸 값을 즉시 원하는 타입으로 사용할 수 있다.
- `Node` 를 내부 정적 클래스(`static class Node<E>`)로 선언해, `MyLinkedListV3` 와 독립적으로 타입 매개변수를 갖도록 했다.

> 이렇게 해서 **자바의 `LinkedList<E>` 와 유사한 형태**의 자료구조가 완성되었다.

---

## 전체 흐름 요약

| 버전 | 한 일 | 남은 문제 → 다음 단계 |
|:----:|-------|----------------------|
| **Node** | 노드의 구조와 연결 이해 (item + next 참조) | 직접 노드를 조작하는 건 불편함 |
| **V1** | 노드 체인을 감싼 리스트, add/get/set/indexOf/size | addLast만 가능, 삽입/삭제 없음 |
| **V2** | 중간 삽입 `add(index, e)`, 삭제 `remove(index)` | `Object` 기반이라 타입 불안전 + 다운캐스팅 필요 |
| **V3** | 제네릭 `<E>` + `Node<E>` 도입 | 타입 안전 + 캐스팅 제거 → **LinkedList 완성** |

### 핵심 교훈

- **LinkedList = 노드 체인**: 배열 대신 노드가 서로를 가리키는 구조로, 크기 제한이 없고 삽입/삭제 시 데이터를 밀 필요가 없다.
- **맨 앞 삽입/삭제가 O(1)**: 참조 교체만으로 완료되므로, 맨 앞(head)에서의 추가·제거는 배열보다 훨씬 효율적이다.
- **조회는 O(n)**: 배열처럼 인덱스로 메모리 주소를 바로 계산할 수 없고, 항상 첫 노드부터 따라가야 한다.
- **ArrayList vs LinkedList 선택 기준**:
  - 조회가 많고 삽입/삭제가 적다면 → `ArrayList` (get O(1))
  - 앞쪽 삽입/삭제가 많고 조회가 적다면 → `LinkedList` (addFirst/removeFirst O(1))
  - 실무에서는 대부분 `ArrayList` 가 더 빠르다 — 현대 CPU의 캐시 효율(메모리 연속성) 덕분에 O(n) 이동 비용이 예상보다 작기 때문이다.
