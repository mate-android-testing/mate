package org.mate.datagen;

import java.io.File;
import java.io.InputStream;

/**
 * Created by marceloeler on 14/03/17.
 */

public class PDictionary {
    private static String str1 = "The Treasury Committee's action against Charlotte Hogg is certainly significant.\n" +
            "And its significance goes well beyond the future of Ms Hogg.\n" +
            "Mark Carney, the Governor of the Bank of England, backed her until one minute to midnight.\n" +
            "Which, after the evidence laid out by the committee today, is likely to be questioned by MPs when Mr Carney next appears before them.\n" +
            "Read more from Kamal here.\n" +
            "'Honest mistake'\n" +
            "Bank governor Mark Carney said: \"While I fully respect her decision, taken in accordance with her view of what was the best for this institution, I deeply regret that Charlotte Hogg has chosen to resign from the Bank of England.\n" +
            "\"We will do everything we can to honour her work for the people of the United Kingdom by building on her contributions.\"\n" +
            "In her resignation letter, Ms Hogg said she was very sorry for failing to disclose her brother's role.\n" +
            "She also said she had offered to resign last week.\n" +
            "\"It was an honest mistake: I have made no secret of my brother's job - indeed it was I who informed the Treasury Select Committee of it, before my hearing.\n" +
            "\"But I fully accept it was a mistake, made worse by the fact that my involvement in drafting the policy made it incumbent on me to get all my own declarations absolutely right,\" Ms Hogg said.\n" +
            "\"I also, in the course of a long hearing, unintentionally misled the committee as to whether I had filed my brother's job on the correct forms at the Bank.\n" +
            "\"I would like to repeat my apologies for that, and to make clear that the responsibility for all those errors is mine alone.\"\n" +
            "The Bank is planning to tighten up its governance of the code of conduct.\n" +
            "'More to do'\n" +
            "Andrew Tyrie, chairman of the Treasury Committee, said: \"This is a regrettable business with no winners. Ms Hogg has acted in the best interest of the institution for which she has been working. This is welcome.\n" +
            "\"It is also welcome that the Bank has responded immediately by announcing an internal review.\n" +
            "\"The Bank's governance is already in much better shape than it was a few years ago. It is something to which the Governor and Court has been committed for some time. But there is clearly more to do.\"\n" +
            "John Mann, an MP on the Treasury Committee, told BBC Radio 5 live it was \"appropriate\" she had resigned.\n" +
            "\"She wrote the code of conduct for the Bank of England which says: 'Do you have a family member working in the banking industry, ie a brother or sister?'\n" +
            "\"It's an explicit set of standards that she wrote and didn't comply with and that made her position totally untenable.\n" +
            "\"The question for Mark Carney is why the set of standards wasn't being adhered to. He needs to be very clear now on what the Bank's going to do to address this. It's clearly a systemic issue in the Bank.\""+
            "";

    private static String str2 = "Lorem ipsum dolor sit amet, libero eu aliquam velit quam in, vivamus mauris non id amet, eum sagittis dolor erat arcu eu id, nam quis. Curabitur ut sed porttitor. Vel in in urna ultricies massa, vivamus maecenas volutpat, dapibus sodales ipsum nullam sed magnis, id leo. Libero purus augue nisl mattis, fermentum arcu risus nulla quam, faucibus dui at est tempor mauris. Sagittis platea nunc lectus, fusce ornare, vulputate arcu, suspendisse et purus nec. Porttitor nec sem, cursus quisque vivamus justo elit. Eu et ipsum leo non leo tortor. Quisque pretium sed, varius egestas nunc quis lacus, bibendum eleifend, commodo phasellus bibendum urna sem donec. Sit vestibulum in wisi at vehicula odio, hac ad eu non montes sodales nullam, enim consectetuer in eget sodales, eu sit vehicula pellentesque, nec elementum sem fusce. Tempor a blandit sed.\n" +
            "At in natoque in in dolor, mus iaculis sit, sem ligula, donec donec vel sed, morbi morbi tincidunt libero mauris. Vulputate augue facilisis pretium non pulvinar blandit, augue dui lacus. Praesent curabitur, aptent habitasse posuere vestibulum per, eget amet id vestibulum quis, in sed nunc vitae, orci turpis sed arcu accumsan. Erat massa. Sollicitudin et ac arcu, vitae fringilla.\n" +
            "Quisque eget, risus sociis venenatis amet libero wisi, tellus ipsum nam ratione nascetur amet ut, at erat aliquam aliquam at cursus. Ut integer et amet wisi placerat, enim dapibus congue. Nunc a leo, duis dolor. Massa tristique, nec sed eget purus vel, praesent at consectetuer. A molestie, sagittis justo et dictum nullam mauris mauris. Arcu dui non faucibus vel, eget suscipit vestibulum vestibulum nullam, wisi commodo suscipit. Ut est, non potenti mauris imperdiet justo blandit, ligula neque sed aenean, sed etiam dolores a, eget risus tristique tellus risus.\n" +
            "Suspendisse ut elementum egestas nec dignissim per, posuere viverra a tellus sapien, dolor a wisi vitae, tortor pede lectus faucibus mauris aliquam. Consequat malesuada nullam mauris mi, fermentum sit sem leo. Mus quis leo amet egestas donec imperdiet, mauris proin pretium mollis integer imperdiet quis, justo lectus vitae mauris, nibh dolor ante a donec, tincidunt interdum et ac consectetuer. Tellus lacus. Vestibulum at vivamus nunc ullamcorper odio nam, eu mauris non amet lectus molestie, etiam convallis in molestie varius wisi etiam, morbi nibh tempor, aenean fermentum pede egestas. Ultricies eleifend wisi scelerisque auctor, placerat pharetra mi nunc.";

    public static String[] getWords(){


        return str1.replace("'","").split(" ");
    }

}
