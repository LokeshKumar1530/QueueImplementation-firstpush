import java.util.Scanner;
class Main
{
    public static void main(String [] logu)
    {
        Queue queue = new Queue();
        Scanner scan = new Scanner(System.in);
        int n = scan.nextInt();
        for(int i = 1; i <=n; i++)
        {
            queue.push(i*10);
        }

        int length = queue.length();
        for(int i = 0; i < length; i++)
        {
            System.out.print(queue.pull() + " ");
        }
    }
}
class Queue
{
    private Node front;
    private Node rear;
    private int size;
    
    public Queue()
    {
        front = null;
        rear = null;
        size = 0;
    }

    public void push(int data)
    {
        Node newNode = new Node(data);
        if(front == null && rear == null)
        {
            front = newNode;
            rear = newNode;
        }
        else
        {
            rear.next = newNode;
            rear = newNode;
        }
        size++;
    }

    public int pull()
    {
        if(front == null)
        {
            System.out.println("There is empty queue");
            return -1;
        }
        int result = front.data;
        front = front.next;
        size--;
        return result;
    }

    public int length()
    {
        return size;
    }



}
class Node
{
    int data;
    Node next;

    Node(int data)
    {
        this.data = data;
        next = null;
    }
}